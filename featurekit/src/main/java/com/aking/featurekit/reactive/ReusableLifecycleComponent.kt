package com.aking.featurekit.reactive

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 可重用的生命周期感知视图组件基类
 *
 * 主要特性：
 * - 创建时自动初始化生命周期
 * - attach 时智能检查是否需要重新初始化
 * - detach 时完全销毁生命周期
 * - 支持重复使用
 *
 * 工作原理：
 * 1. 实例创建时初始化为 CREATED 状态
 * 2. View attach 时检查当前状态，如果是 DESTROYED 则重新初始化
 * 3. 然后进入 STARTED -> RESUMED 状态
 * 4. View detach 时完整地销毁：PAUSED -> STOPPED -> DESTROYED
 * 5. 下次 attach 时会重新从 CREATED 开始
 *
 * 使用示例：
 * ```kotlin
 * class MyComponent : ReusableLifecycleComponent() {
 *     init {
 *         lifecycle.addObserver(object : DefaultLifecycleObserver {
 *             override fun onCreate(owner: LifecycleOwner) {
 *                 println("组件初始化")
 *             }
 *
 *             override fun onResume(owner: LifecycleOwner) {
 *                 println("组件可见")
 *             }
 *
 *             override fun onDestroy(owner: LifecycleOwner) {
 *                 println("组件销毁")
 *             }
 *         })
 *     }
 * }
 *
 * // 使用
 * val component = MyComponent()
 * view.addOnAttachStateChangeListener(component)
 * ```
 *
 * 生命周期状态转换：
 * ```
 * 创建实例: CREATED
 * View attach: CREATED -> STARTED -> RESUMED (如果之前未销毁)
 *             或 DESTROYED -> CREATED -> STARTED -> RESUMED (如果之前已销毁)
 * View detach: RESUMED -> PAUSED -> STOPPED -> DESTROYED
 * ```
 *
 * @author Ak
 * @since 2025/6/30 9:28
 * @see LifecycleOwner
 * @see View.OnAttachStateChangeListener
 */
abstract class ReusableLifecycleComponent : View.OnAttachStateChangeListener, LifecycleOwner {

    /**
     * 生命周期注册表的私有引用
     */
    private var _lifecycleRegistry: LifecycleRegistry? = null

    /**
     * 生命周期注册表，用于管理生命周期状态
     * 使用延迟初始化模式
     */
    private val lifecycleRegistry: LifecycleRegistry
        get() = _lifecycleRegistry ?: LifecycleRegistry(this).also {
            _lifecycleRegistry = it
        }

    /**
     * 获取当前组件的生命周期对象
     *
     * @return 当前组件的 Lifecycle 实例
     */
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    init {
        initLifecycle()
    }

    /**
     * 初始化生命周期
     *
     * 私有方法，将生命周期状态设置为 CREATED。
     * 在实例创建时和需要重新初始化时调用。
     */
    private fun initLifecycle() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    /**
     * 当 View 附加到窗口时的回调
     *
     * 该方法会：
     * 1. 检查当前生命周期状态，如果已销毁则重新初始化
     * 2. 触发 ON_START 事件 - 组件开始可见
     * 3. 触发 ON_RESUME 事件 - 组件完全可见并可交互
     *
     * @param v 被附加到窗口的 View
     */
    override fun onViewAttachedToWindow(v: View) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            initLifecycle()
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * 当 View 从窗口分离时的回调
     *
     * 该方法会依次触发完整的销毁流程：
     * 1. ON_PAUSE 事件 - 组件暂停，不再可交互
     * 2. ON_STOP 事件 - 组件停止，不再可见
     * 3. ON_DESTROY 事件 - 组件完全销毁
     *
     * 销毁后，组件可以在下次 attach 时重新初始化。
     *
     * @param v 从窗口分离的 View
     */
    override fun onViewDetachedFromWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

}