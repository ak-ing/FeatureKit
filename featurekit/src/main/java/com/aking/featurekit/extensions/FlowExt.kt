package com.aking.featurekit.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * @author Ak
 * 2025/6/7  11:41
 */

/**
 * 在生命周期范围内安全地收集 Flow 数据
 * 自动处理生命周期感知的数据收集，避免内存泄漏，简化 Flow 收集代码
 *
 * 注意：此方法的行为与 LiveData 一致：
 * 1. 生命周期感知：
 *    - 仅在生命周期处于活跃状态（默认为 STARTED）时才会收集数据
 *    - 注册观察时会重播最新数据，确保UI状态同步
 * 2. 数据去重：
 *    - 使用 distinctUntilChanged 确保数据实际变化时才触发
 *    - 避免生命周期转换到 STARTED 状态时重复触发相同数据
 * 3. 生命周期绑定：
 *    - 自动在生命周期销毁时取消收集
 *    - 防止内存泄漏，无需手动取消
 *
 * @param lifecycleOwner 生命周期所有者，用于绑定数据收集的生命周期范围
 * @param minActiveState 最小活跃状态，默认为 [Lifecycle.State.STARTED]
 * @param collector 数据收集器，用于处理收集到的数据
 */
fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) = lifecycleOwner.lifecycleScope.launch {
    flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
        .distinctUntilChanged()
        .collect(collector)
}

/** @see collectWithLifecycle */
fun <T> Flow<T>.collectWithLifecycle(
    fragment: Fragment,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) = fragment.viewLifecycleOwner.lifecycleScope.launch {
    flowWithLifecycle(fragment.viewLifecycleOwner.lifecycle, minActiveState)
        .distinctUntilChanged()
        .collect(collector)
}
