package com.aking.featurekit.language

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 任务执行策略：跳过执行中的任务
 *
 * 当前一个任务正在执行时，新的调用会被直接跳过。
 * 适合处理那些重复触发没有意义，需要互斥执行的场景。
 *
 * 特点：
 * - 任务互斥：同一时间只允许一个任务执行
 * - 跳过处理：执行中时新的调用直接跳过
 * - 自动丢弃：不保存也不排队等待
 *
 * 适用场景：
 * - 点击防抖：防止按钮快速重复点击
 * - 资源互斥：需要互斥访问的资源操作
 * - 状态刷新：正在刷新时忽略新的刷新请求
 *
 * 使用示例：
 * ```
 * class RefreshFeature {
 *     private val refresh = debounce(viewModelScope) {
 *         // 正在刷新时的新刷新请求会被跳过
 *         repository.refresh()
 *     }
 *
 *     fun onRefresh() {
 *         refresh() // 如果正在刷新，此次调用会被跳过
 *     }
 * }
 * ```
 *
 * @param coroutineScope 协程作用域，用于控制任务的生命周期
 * @param callback 实际要执行的挂起函数
 * @return 返回一个函数，调用时如果有任务在执行则跳过
 */
fun debounce(
    coroutineScope: CoroutineScope,
    callback: suspend () -> Unit
): () -> Unit {
    var job: Job? = null
    return {
        if (job?.isActive != true) {
            job = coroutineScope.launch { callback() }
        }
    }
}

/**
 * 任务执行策略：中断前一个任务
 *
 * 通过取消前一个任务来确保新任务可以立即执行，每次调用时：
 * 1. 如果有正在执行的任务，立即取消
 * 2. 立即开始执行新任务
 *
 * 特点：
 * - 中断性：主动取消当前任务
 * - 即时性：新任务立即执行
 * - 非阻塞：不等待任务完成
 *
 * 适用场景：
 * - 实时搜索：新输入时取消当前搜索，立即开始新搜索
 * - 网络请求：新请求时取消旧请求，避免结果混乱
 * - 耗时计算：新数据到达时停止当前计算，立即处理新数据
 *
 * 使用示例：
 * ```
 * class SearchViewModel : ViewModel() {
 *     private val _searchResult = MutableStateFlow<List<String>>(emptyList())
 *     val searchResult = _searchResult.asStateFlow()
 *
 *     // 创建搜索处理函数
 *     private val search = debounceCancelBefore(viewModelScope) {
 *         // 新搜索开始时会取消正在进行的搜索
 *         val result = withContext(Dispatchers.IO) {
 *             repository.searchItems(query)  // 耗时的网络请求
 *         }
 *         _searchResult.value = result
 *     }
 *
 *     fun onQueryChanged(newQuery: String) {
 *         query = newQuery
 *         search() // 取消当前搜索，立即开始新搜索
 *     }
 * }
 * ```
 *
 * @param coroutineScope 协程作用域，用于控制任务的生命周期
 * @param callback 实际要执行的挂起函数
 * @return 返回一个函数，调用时会中断之前的任务并立即执行新任务
 */
fun debounceCancelBefore(
    coroutineScope: CoroutineScope,
    callback: suspend () -> Unit
): () -> Unit {
    var job: Job? = null
    return {
        job?.cancel()
        job = coroutineScope.launch { callback() }
    }
}

/**
 * 任务执行策略：保留最新值
 *
 * 确保最新的值一定会被处理。当任务正在执行时，新的值会被保存，
 * 等待当前任务完成后立即使用最新值执行。适合只关心最终状态的场景。
 *
 * 特点：
 * - 值保证：保证最新值一定被处理
 * - 执行控制：等待当前任务完成
 * - 状态合并：中间值自动丢弃
 *
 * 适用场景：
 * - 拖拽保存：只保存最终位置
 * - 配置同步：只同步最新配置
 * - 状态更新：只更新最终状态
 *
 * 使用示例：
 * ```
 * class ConfigSyncFeature {
 *     private val syncConfig = debounceRetainNewest(viewModelScope) { config: Config ->
 *         // 只会同步最新的配置，中间的配置变更会被丢弃
 *         repository.syncConfig(config)
 *     }
 *
 *     fun onConfigChange(config: Config) {
 *         updateLocalConfig(config) // 立即更新本地显示
 *         syncConfig(config) // 保证最新配置最终会被同步
 *     }
 * }
 * ```
 *
 * @param T 输入参数类型
 * @param coroutineScope 协程作用域，用于控制任务的生命周期
 * @param callback 实际要执行的挂起函数，接收类型为T的参数
 * @return 返回一个函数，接收T类型参数，用于触发操作
 */
fun <T> debounceRetainNewest(
    coroutineScope: CoroutineScope,
    callback: suspend (T) -> Unit
): (T) -> Unit {
    var job: Job? = null
    var awaitFun: (suspend () -> Unit)? = null
    return { t ->
        awaitFun = {
            callback(t)
            if (awaitFun != null) {
                job = coroutineScope.launch {
                    awaitFun?.invoke()
                }
                awaitFun = null
            }
        }
        if (job?.isActive != true) {
            job = coroutineScope.launch {
                awaitFun?.let {
                    awaitFun = null
                    it()
                }
            }
        }
    }
}
