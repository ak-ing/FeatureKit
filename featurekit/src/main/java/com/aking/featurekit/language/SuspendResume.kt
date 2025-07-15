package com.aking.featurekit.language

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Ak
 * 2025/6/20  17:06
 */

/**
 * 用于实现协程的挂起和恢复控制，常用于需要异步等待和手动触发恢复的场景
 */
interface SuspendResumeController {
    /**
     * 检查当前控制器是否处于挂起状态。
     * @return 如果已挂起，则返回 true。
     */
    val isPaused: Boolean

    /**
     * 挂起当前协程，直到 [resume] 被调用。
     * 如果当前控制器已处于暂停状态，则什么也不做。
     */
    suspend fun wait()

    /**
     * 恢复挂起的协程。
     * 如果没有协程被挂起，则什么也不做。
     */
    fun resume()
}

/**
 * 一个可重用的协程控制器，允许在“暂停”和“运行”状态之间切换。
 *
 * 该控制器可以用于协程间的同步，允许一个协程等待直到另一个协程或普通代码显式触发恢复
 *
 * 使用示例:
 * ```
 * val signal = suspendResumeSignal()
 *
 * // 在协程中等待
 * launch {
 *     signal.wait() // 此处协程将被挂起
 *     // 当 resume() 被调用后，这里的代码才会继续执行
 * }
 *
 * // 在其他地方触发恢复
 * signal.resume() // 恢复之前挂起的协程
 * ```
 *
 * @return 返回一个 [SuspendResumeController] 实例
 */
fun reusableSuspendResumeSignal() = object : SuspendResumeController {
    override val isPaused: Boolean
        get() = continuation != null
    private var continuation: CancellableContinuation<Unit>? = null

    override suspend fun wait() {
        if (isPaused) return
        suspendCancellableCoroutine { cont ->
            continuation = cont
        }
    }

    override fun resume() {
        continuation?.let {
            it.resume(Unit)
            continuation = null
        }
    }
}