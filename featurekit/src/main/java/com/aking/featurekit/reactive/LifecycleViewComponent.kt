package com.aking.featurekit.reactive

import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * 生命周期感知的视图组件基类
 *
 * 该抽象类提供了一个与View的attach/detach状态绑定的生命周期管理机制。
 * 当View附加到窗口时，生命周期会自动进入STARTED和RESUMED状态；
 * 当View从窗口分离时，生命周期会自动进入PAUSED和STOPPED状态。
 *
 * 主要特性：
 * - 自动管理生命周期状态转换
 * - 与View的attach/detach状态同步
 * - 支持生命周期观察者模式
 * - 提供手动释放资源的方法
 *
 * 使用方法：
 * 1. 继承此类并实现具体的组件逻辑
 * 2. 在需要监听View状态的地方调用view.addOnAttachStateChangeListener(component)
 * 3. 在组件不再需要时调用release()方法释放资源
 *
 * 示例代码：
 * ```kotlin
 * class MyComponent : LifecycleViewComponent() {
 *     init {
 *         lifecycle.addObserver(object : DefaultLifecycleObserver {
 *             override fun onStart(owner: LifecycleOwner) {
 *                 // 组件启动逻辑
 *             }
 *
 *             override fun onStop(owner: LifecycleOwner) {
 *                 // 组件停止逻辑
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
 * - 初始化：CREATED
 * - View附加到窗口：CREATED -> STARTED -> RESUMED
 * - View从窗口分离：RESUMED -> STARTED -> CREATED
 * - 调用release()：CREATED -> DESTROYED
 *
 * @author Ak
 * @since 2025/6/17 17:50
 * @see LifecycleOwner
 * @see OnAttachStateChangeListener
 */
abstract class LifecycleViewComponent : OnAttachStateChangeListener, LifecycleOwner {

    /**
     * 生命周期注册表的私有引用，延迟初始化
     */
    private var _lifecycleRegistry: LifecycleRegistry? = null

    /**
     * 生命周期注册表，用于管理生命周期状态
     * 采用延迟初始化模式，确保在需要时才创建实例
     */
    private val lifecycleRegistry: LifecycleRegistry
        get() = _lifecycleRegistry ?: LifecycleRegistry(this).also {
            _lifecycleRegistry = it
        }

    /**
     * 获取当前组件的生命周期对象
     *
     * @return 当前组件的Lifecycle实例
     */
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    init {
        initLifecycle()
    }

    /**
     * 初始化生命周期
     *
     * 私有方法，防止外部调用导致重复初始化。
     * 在初始化时将生命周期状态设置为CREATED。
     */
    private fun initLifecycle() {
        _lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    /**
     * 当View附加到窗口时的回调
     *
     * 该方法会依次触发以下生命周期事件：
     * 1. ON_START - 组件开始可见
     * 2. ON_RESUME - 组件完全可见并可交互
     *
     * @param v 被附加到窗口的View
     */
    override fun onViewAttachedToWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    /**
     * 当View从窗口分离时的回调
     *
     * 该方法会依次触发以下生命周期事件：
     * 1. ON_PAUSE - 组件暂停，不再可交互
     * 2. ON_STOP - 组件停止，不再可见
     *
     * @param v 从窗口分离的View
     */
    override fun onViewDetachedFromWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    /**
     * 释放组件资源
     *
     * 当组件不再需要使用时，应该调用此方法来释放相关资源。
     * 该方法会：
     * 1. 触发ON_DESTROY生命周期事件
     * 2. 清空生命周期注册表引用，防止内存泄漏
     *
     * 注意：调用此方法后，组件将无法再次使用，需要重新创建实例。
     */
    fun release() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        _lifecycleRegistry = null
    }

}