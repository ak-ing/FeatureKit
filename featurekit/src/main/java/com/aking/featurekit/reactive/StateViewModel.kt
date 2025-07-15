package com.aking.featurekit.reactive

import android.util.Log
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aking.featurekit.extensions.TAGHash
import com.aking.featurekit.extensions.collectWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * 一个基于状态管理的 ViewModel 基类
 * @param S 状态数据类型
 * @param initialState 初始状态
 * 主要功能:
 * 1. 管理和维护 UI 状态
 * 2. 提供状态更新机制
 * 3. 自动处理订阅者生命周期
 * 4. 支持 Activity/Fragment/Any LifecycleOwner 中的响应式更新
 * @author Ak
 * 2025/6/17  14:56
 */
abstract class StateViewModel<S, I : Intent>(private val initialState: S) : ViewModel(), Reducer<I> {
    private var initializeCalled = false
    private val uiState = MutableStateFlow(initialState)
    val stateFlow: StateFlow<S> get() = uiState

    /**
     * This function is idempotent provided it is only called from the UI thread.
     */
    @MainThread
    fun initialize() {
        if (initializeCalled) return
        initializeCalled = true
        onInitialize()
        // 监听订阅数量变化，自动触发 active/inactive 回调
        uiState.subscriptionCount
            .map { count -> count > 0 } // map count into active/inactive flag
            .distinctUntilChanged()// only react to true<->false changes
            .onEach { isActive ->
                if (isActive) onActive() else onInactive()
            }
            .catch { throw it }
            .launchIn(viewModelScope)
    }

    /**
     * 设置响应式组件，建立状态更新管道
     * @param reactive 响应式组件，可以是 Fragment、Activity 或其他 LifecycleOwner
     */
    fun setUp(reactive: Reactive<S>) {
        initialize()
        when (reactive) {
            is Fragment -> seedPipeline(reactive.viewLifecycleOwner, reactive)
            is LifecycleOwner -> seedPipeline(reactive, reactive)
            else -> error("reactive must be a Fragment or an instance of LifecycleOwner (e.g., an Activity)")
        }
    }

    override fun reducer(intent: I) {
        intent.handlerIntent()
    }


    /**
     * 当初始化时执行，只会执行一次
     */
    protected open fun onInitialize() {}

    /**
     * 当状态流获得第一个订阅者时调用
     * 通常用于初始化资源或开始某些操作
     */
    protected open fun onActive() {}

    /**
     * 当状态流失去所有订阅者时调用
     * 通常用于释放资源或停止某些操作
     */
    protected open fun onInactive() {}

    /**
     * 处理意图并更新状态
     *
     * 子类需要实现此方法来定义具体的状态转换逻辑。
     */
    protected abstract fun I.handlerIntent()

    /**
     * 初始化状态生产管道
     * @param lifecycleOwner 生命周期所有者，用于绑定状态流的生命周期
     * @param reactive Reactive 实例，用于渲染状态
     */
    private fun seedPipeline(lifecycleOwner: LifecycleOwner, reactive: Reactive<S>) {
        val stateDiff = StateDiff(initialState)
        stateFlow.collectWithLifecycle(lifecycleOwner, collector = { state ->
            stateDiff.update(state) {
                reactive.render(state, stateDiff)
            }
        })
    }

    /** 内部实现，供 LifecycleOwner 调用，用于观察状态的特定部分。 */
    internal fun <T> LifecycleOwner.innerOnEach(keySelector: (S) -> T, action: suspend (value: T) -> Unit) {
        stateFlow
            .map(keySelector)
            .collectWithLifecycle(this, collector = action)
    }

    /**
     * 更新状态
     *
     * 示例：
     * ```kotlin
     * data class MyState(val count: Int = 0, val name: String = "")
     *
     * // ... 在 StateViewModel 子类中 ...
     * fun incrementCount() {
     *     update { copy(count = this.count + 1) }
     * }
     *
     * fun updateName(newName: String) {
     *     update { copy(name = newName) }
     * }
     * ```
     * @param reducer 状态更新函数，接收当前状态并返回新的状态
     */
    protected fun update(reducer: S.() -> S) {
        uiState.update(reducer)
    }

    /**
     * 设置新的状态
     * @param state 新的状态值
     */
    protected fun setState(state: S) {
        uiState.value = state
    }

    /**
     * 在当前状态 [S] 的上下文中执行给定的 [block]。
     * 这允许简洁地访问当前状态的属性和方法。
     *
     * 示例：
     * ```kotlin
     * withState {
     *     // 'this' 指向当前状态对象
     *     if (this.count > 10) {
     *         // 执行某些操作
     *     }
     * }
     * ```
     * @param block 要在当前状态下执行的代码块。
     */
    inline fun withState(block: (S) -> Unit) {
        block(stateFlow.value)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAGHash, "onCleared")
    }
}


/**
 * Simple
 * ``` kotlin
 *
 *
 * sealed class SimpleIntent : Intent {
 *     data class DoSomeThing(val args: String) : SimpleIntent()
 * }
 *
 * class SimpleStateViewModel : StateViewModel<SimpleState>(SimpleState()) {
 *     override fun onActive() {
 *     }
 *
 *     override fun onInactive() {
 *     }
 *
 *     override fun Intent.handlerIntent() {
 *         when (this) {
 *             is SimpleIntent.DoSomeThing -> {
 *                 update { copy(count = count + 1, name = args) }
 *             }
 *         }
 *     }
 * }
 *
 * class SimpleUI : Fragment(), Reactive<SimpleState> {
 *
 *     private val viewModel by activityViewModels<SimpleStateViewModel>()
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         viewModel.setUp(this)
 *     }
 *
 *     override suspend fun render(state: SimpleState, diff: StateDiff<SimpleState>) {
 *         diff({ it.name }) {
 *
 *         }
 *         diff({ it.count }) {
 *
 *         }
 *     }
 * }
 *
 * ```
 */
data class SimpleState(val count: Int = 0, val name: String = "")