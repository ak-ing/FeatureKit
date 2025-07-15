package com.aking.featurekit.extensions.view

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * View点击防抖扩展
 *
 * 适用场景：
 * - 按钮快速点击防抖
 * - 表单提交防重复
 * - 任何需要防止重复点击的场景
 *
 * @author AK
 * @since 2025/6/7
 */

/**
 * 为 View 添加时间间隔防抖
 *
 * @param intervalMillis 两次有效点击之间的最小时间间隔（毫秒）
 * @param onClick 点击回调
 *
 * 使用示例：
 * ```kotlin
 * button.setOnClickDebounce(1000L) {
 *     // 1秒内的重复点击会被忽略
 *     submit()
 * }
 * ```
 */
fun View.setOnClickDebounce(
    intervalMillis: Long = 500,
    onClick: () -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= intervalMillis) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * 为 View 添加协程防抖处理
 * 当前点击回调执行过程中，会跳过新的点击
 *
 * @param lifecycleOwner 生命周期所有者，用于自动取消监听
 * @param onClick 点击回调，执行过程中会跳过新的点击
 *
 * 使用示例：
 * ```kotlin
 * button.setOnClickDebounceByCoroutine(viewLifecycleOwner) {
 *     // 执行耗时操作，期间的点击会被跳过
 *     submitForm()
 * }
 * ```
 */
fun View.setOnClickDebounceByCoroutine(
    lifecycleOwner: LifecycleOwner,
    onClick: suspend () -> Unit
) {
    var job: Job? = null
    setOnClickListener {
        if (job?.isActive != true) {
            job = lifecycleOwner.lifecycleScope.launch {
                onClick()
            }
        }
    }
}