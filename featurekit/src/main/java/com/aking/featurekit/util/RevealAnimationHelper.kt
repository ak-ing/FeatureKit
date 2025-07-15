package com.aking.featurekit.util

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.view.drawToBitmap
import kotlin.math.sqrt

/**
 * 揭露动画工具类
 * 提供切换图片时的过渡动画效果
 */
object RevealAnimationHelper {

    /**
     * 执行揭露动画
     *
     * @param targetView 要应用动画的目标视图（通常是壁纸视图）
     * @param onUpdate 动画开始前的操作回调（如换肤、更新UI等）
     * @param animationDuration 动画时长，默认900ms
     * @param onComplete 动画完成回调
     */
    fun startSkinTransitionAnimation(
        targetView: View,
        onUpdate: () -> Unit,
        animationDuration: Long = 900L,
        onComplete: (() -> Unit)? = null
    ) {
        val context = targetView.context
        val parentView = targetView.parent as? ViewGroup ?: return

        // 1. 保存旧壁纸的截图
        val oldBitmap = try {
            targetView.drawToBitmap()
        } catch (e: Exception) {
            null
        }

        // 2. 创建旧壁纸的ImageView作为底层背景
        val oldImageView = createOldImageView(context, oldBitmap)

        // 将oldImageView插入到targetView的底层
        val targetIndex = parentView.indexOfChild(targetView)
        parentView.addView(
            oldImageView,
            targetIndex,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // 3. 执行换肤操作，更新targetView到新壁纸
        onUpdate()

        // 4. 开始揭露动画 - targetView从中心圆形扩展到全屏
        startCircularRevealAnimation(
            targetView = targetView,
            oldImageView = oldImageView,
            parentView = parentView,
            duration = animationDuration,
            onComplete = onComplete
        )
    }

    /**
     * 创建承载旧壁纸的ImageView
     */
    private fun createOldImageView(context: Context, bitmap: Bitmap?): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            bitmap?.let { setImageBitmap(it) }
        }
    }

    /**
     * 执行圆形揭露动画
     */
    private fun startCircularRevealAnimation(
        targetView: View,
        oldImageView: ImageView,
        parentView: ViewGroup,
        duration: Long,
        onComplete: (() -> Unit)?
    ) {
        val centerX = targetView.width / 2
        val centerY = targetView.height / 2
        val finalRadius = calculateFinalRadius(centerX, centerY)

        // 使用系统的圆形揭露动画 (API 21+)
        val revealAnimator = ViewAnimationUtils.createCircularReveal(
            targetView,
            centerX,
            centerY,
            0f,
            finalRadius
        ).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }

        revealAnimator.doOnEnd {
            // 清理底层的旧壁纸视图
            parentView.removeView(oldImageView)
            onComplete?.invoke()
        }

        revealAnimator.start()
    }

    /**
     * 计算最终半径
     */
    private fun calculateFinalRadius(centerX: Int, centerY: Int): Float {
        return sqrt((centerX * centerX + centerY * centerY).toDouble()).toFloat()
    }
}
