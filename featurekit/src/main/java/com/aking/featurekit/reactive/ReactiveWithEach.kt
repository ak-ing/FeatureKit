package com.aking.featurekit.reactive

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

/**
 * 用于在状态变化时执行特定操作的接口.
 * 提供了一个简化的方式来观察状态的特定部分，并在其变化时执行回调。
 *
 * @param S 状态的类型。
 * @author Ak
 * 2025/6/26  16:01
 */
interface ReactiveWithEach<S> : Reactive<S> {

    /**
     * 观察状态 [S] 中的特定部分 [T]。
     * 只有当通过 [keySelector] 提取的部分发生变化时，[action] 才会执行。
     *
     * 例如，如果你的状态是一个包含 `count` 和 `name` 的数据类，你可以像这样只观察 `count`：
     *
    ```kotlin
     * viewmodel.onEach({ it.count }) { newCount ->
     *     // 当 count 变化时执行这里的逻辑
     *     Log.d("StateViewModel", "Count changed to: $newCount")
     * }
     * ```
     *
     * 或者，如果你想观察一个由多个属性组成的自定义对象，你可以让 `keySelector` 返回那个对象：
     *
    ```kotlin
     * data class MyObservedPart(val prop1: String, val prop2: Int)
     *
     * viewmodel.onEach({ MyObservedPart(it.someString, it.someInt) }) { myPart ->
     *    // 当 myPart (即 prop1 或 prop2) 变化时执行这里的逻辑
     *    Log.d("StateViewModel", "Observed part changed: ${myPart.prop1}, ${myPart.prop2}")
     * }
     * ```
     *
     * @param S 状态的类型。
     * @param T 要观察的状态部分的类型。
     * @param keySelector 从状态 [S] 中提取要观察的部分 [T] 的函数。
     * @param action 当观察的部分 [T] 发生变化时执行的回调函数。
     */
    fun <T> StateViewModel<S, *>.onEach(keySelector: (S) -> T, action: suspend (value: T) -> Unit) {
        initialize()
        when (this@ReactiveWithEach) {
            is Fragment -> innerOnEach(keySelector, action)
            is LifecycleOwner -> innerOnEach(keySelector, action)
            else -> error("must call on Fragment or an instance of LifecycleOwner (e.g., an Activity)")
        }
    }

    override suspend fun render(state: S, diff: StateDiff<S>) {
        // 默认实现
    }
}