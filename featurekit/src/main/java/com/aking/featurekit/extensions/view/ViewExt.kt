package com.aking.featurekit.extensions.view

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * @author Ak
 * 2024/9/27 15:35
 */

/**
 * 扩充View的点击区域
 *
 * 通过TouchDelegate实现，将View的可点击区域向四周扩展指定的大小。
 * 注意：
 * 1. 需要在View完成布局后调用，因此在post中执行
 * 2. 扩展区域不能超过父View的边界
 * 3. 同一个容器内只有一个有效
 *
 * 使用示例：
 * ```kotlin
 * // 将按钮的可点击区域向四周扩展20dp
 * button.expendTouchArea(20.dp)
 *
 * // 在RecyclerView的item中使用时，确保在onBindViewHolder中调用
 * override fun onBindViewHolder(holder: ViewHolder, position: Int) {
 *     holder.itemView.post {
 *         holder.button.expendTouchArea(20.dp)
 *     }
 * }
 * ```
 *
 * @param expendSize 要扩展的区域大小，单位为像素
 * @throws IllegalStateException 如果View没有父View或父View不是ViewGroup
 */
fun View.expendTouchArea(expendSize: Int) {
    val parentView = parent as View
    parentView.post {
        val rect = Rect()
        getHitRect(rect) // view构建完成后才能获取，所以放在post中执行

        // 重新指定响应的 rect 区域
        rect.top -= expendSize
        rect.bottom += expendSize
        rect.left -= expendSize
        rect.right += expendSize

        // 设置触摸委托，将扩展的区域委托给当前View处理
        parentView.touchDelegate = TouchDelegate(rect, this)
    }
}

/**
 * sample:
 * ```
 * viewLifecycleOwner.lifecycleScope.launch {
 *     // 将该视图设置为不可见，再设置一些文字
 *     titleView.isInvisible = true
 *     titleView.text = "Hi everyone!"
 *
 *     // 等待下一次布局事件的任务，然后才可以获取该视图的高度
 *     titleView.awaitNextLayout()
 *
 *     // 布局任务被执行
 *     // 现在，我们可以将视图设置为可见，并其向上平移，然后执行向下的动画
 *     titleView.isVisible = true
 *     titleView.translationY = -titleView.height.toFloat()
 *     titleView.animate().translationY(0f)
 * }
 * ```
 * 等待 View 被布局完成
 */
suspend fun View.awaitNextLayout() = suspendCancellableCoroutine<Unit> { cont ->
    // 这里的 lambda 表达式会被立即调用，允许我们创建一个监听器
    val listener = object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            v: View?,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            // 视图的下一次布局任务被调用
            // 先移除监听，防止协程泄漏
            removeOnLayoutChangeListener(this)
            // 最终，唤醒协程，恢复执行
            cont.resume(Unit)
        }

    }
    // 如果协程被取消，移除该监听
    cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
    // 最终，将监听添加到 view 上
    addOnLayoutChangeListener(listener)
    // 这样协程就被挂起了，除非监听器中的 cont.resume() 方法被调用
}