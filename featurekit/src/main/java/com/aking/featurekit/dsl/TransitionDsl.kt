package com.aking.featurekit.dsl

import android.view.ViewGroup
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

/**
 * Android Transition 动画 DSL 封装
 *
 * 提供简单易用的 DSL 方式创建和管理 Transition 动画序列。
 * 支持按序播放和并行播放，可灵活配置动画顺序。
 *
 * @author AK
 * @since 2025/6/7
 */
/**
 * 创建并执行 Transition 动画序列
 *
 * @param reverse 是否反转动画顺序，true表示后添加的动画先播放
 * @param sequential 是否按序播放，false表示并行播放
 * @param transitionBuilder DSL构建动画集合的代码块
 * @param updateViews 动画触发前的视图更新逻辑
 *
 * 使用示例:
 * ```kotlin
 * // 使用DSL方式，顺序执行的转场动画：
 * container.beginDelayedTransition(
 *     sequential = false, // 并行播放
 *     transitionBuilder = {
 *         fade(onEnd = { /* 动画结束回调 */ }){
 *             duration = 300L
 *             addTarget(view1)
 *         }
 *         slide{
 *             slideEdge = Gravity.END
 *             addTarget(view2)
 *         }
 *         transition(
 *             transition = ChangeBounds(),
 *             config = {
 *                 duration = 500L
 *                 addTarget(view3)
 *             }
 *         )
 *     },
 *     updateViews = {
 *         view1.visibility = View.VISIBLE
 *         view2.translationX = 200f
 *         view3.width = newWidth
 *     }
 * )
 * ```
 */
fun ViewGroup.beginDelayedTransition(
    reverse: Boolean = false,
    sequential: Boolean = true,
    transitionBuilder: TransitionScope.() -> Unit,
    updateViews: () -> Unit = {}
) {
    val builder = TransitionScope()
    builder.transitionBuilder()

    // 处理动画顺序反转
    val orderedTransitions = if (reverse) {
        builder.transitions.asReversed()
    } else {
        builder.transitions
    }

    // 结束之前的过渡
    TransitionManager.endTransitions(this)

    // 创建 TransitionSet
    val transitionSet = TransitionSet().apply {
        if (sequential) {
            ordering = TransitionSet.ORDERING_SEQUENTIAL
        }
        orderedTransitions.forEach { addTransition(it) }
    }

    // 开始动画
    TransitionManager.beginDelayedTransition(this, transitionSet)

    // 触发视图更新，触发动画
    updateViews()
}

class TransitionScope {
    internal val transitions = mutableListOf<Transition>()

    /**
     * 添加淡入淡出动画
     * @param onEnd 动画结束回调
     * @param config 动画配置代码块
     */
    fun fade(
        onEnd: (() -> Unit)? = null,
        config: Fade.() -> Unit = {}
    ) {
        val fade = Fade().apply(config)
        onEnd?.let { fade.onEnd(onEnd) }
        transitions.add(fade)
    }

    /**
     * 添加滑动动画
     * @param onEnd 动画结束回调
     * @param config 动画配置代码块
     */
    fun slide(
        onEnd: (() -> Unit)? = null,
        config: Slide.() -> Unit = {}
    ) {
        val slide = Slide().apply(config)
        onEnd?.let { slide.onEnd(onEnd) }
        transitions.add(slide)
    }

    /**
     * 添加自定义转场动画，如 ChangeBounds、Explode
     * 可以添加任意 Transition 子类的实例
     *
     * @param transition 自定义的 Transition 实例
     * @param onEnd 动画结束回调
     * @param config 动画配置代码块
     */
    fun transition(
        transition: Transition,
        onEnd: (() -> Unit)? = null,
        config: Transition.() -> Unit = {}
    ) {
        transition.apply(config)
        onEnd?.let { transition.onEnd(it) }
        transitions.add(transition)
    }

    // 可扩展：添加其他动画类型，如 ChangeBounds、Explode 等
}

/**
 * 添加动画结束回调
 * @param onEnd 动画结束时执行的回调函数
 */
fun Transition.onEnd(
    onEnd: () -> Unit
): Transition {
    addListener(object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) = Unit
        override fun onTransitionEnd(transition: Transition) = onEnd()
        override fun onTransitionCancel(transition: Transition) = Unit
        override fun onTransitionPause(transition: Transition) = Unit
        override fun onTransitionResume(transition: Transition) = Unit
    })
    return this
}

/**
 * 添加动画开始回调
 * @param onStart 动画开始时执行的回调函数
 */
fun Transition.onStart(
    onStart: () -> Unit
): Transition {
    addListener(object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) = onStart()
        override fun onTransitionEnd(transition: Transition) = Unit
        override fun onTransitionCancel(transition: Transition) = Unit
        override fun onTransitionPause(transition: Transition) = Unit
        override fun onTransitionResume(transition: Transition) = Unit
    })
    return this
}