package com.aking.featurekit.extensions.view

/**
 * Created by AK on 2024-03-28.
 * Description: RecyclerView常用间距、分割线等装饰器扩展
 */

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 为 RecyclerView 添加统一的边距装饰
 *
 * 特点：
 * 1. 支持布局类型：
 *    - LinearLayoutManager：线性布局（垂直/水平）
 *    - GridLayoutManager：网格布局
 * 2. 灵活的边距控制：
 *    - 可单独设置上下左右边距
 *    - 支持忽略首个项目（线性布局）或首行（网格布局）的边距
 * 3. 边距应用规则：
 *    - 线性布局：每个 item 都会应用设置的边距
 *    - 网格布局：考虑 spanCount，每个 item 都会获得相同的边距
 *
 * 使用示例：
 * ```kotlin
 * // 1. 线性布局设置统一边距
 * recyclerView.addSpacingDecoration(16.dp)
 *
 * // 2. 设置不同的上下左右边距
 * recyclerView.addSpacingDecoration(
 *     left = 8.dp,
 *     top = 16.dp,
 *     right = 8.dp,
 *     bottom = 16.dp
 * )
 *
 * // 3. 网格布局，忽略首行的边距
 * recyclerView.addSpacingDecoration(
 *     left = 8.dp,
 *     top = 16.dp,
 *     right = 8.dp,
 *     bottom = 16.dp,
 *     expectFirst = true // 首行不会应用边距
 * )
 * ```
 *
 * 注意事项：
 * 1. 边距值应考虑设备屏幕大小和密度，建议使用 dp 单位
 * 2. 如果 RecyclerView 已经设置了 padding，需要考虑叠加效果
 * 3. expectFirst 参数在不同布局管理器中的行为：
 *    - LinearLayoutManager：仅忽略第一个 item 的边距
 *    - GridLayoutManager：忽略第一行所有 item 的边距
 *
 * @param left 左边距，单位为像素，默认为0
 * @param top 上边距，单位为像素，默认为0
 * @param right 右边距，单位为像素，默认为0
 * @param bottom 下边距，单位为像素，默认为0
 * @param expectFirst 是否忽略第一个项目（线性布局）或第一行（网格布局）的边距，默认为false
 *
 * @see RecyclerView.ItemDecoration
 * @see LinearLayoutManager
 * @see GridLayoutManager
 */
fun RecyclerView.addSpacingDecoration(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
    expectFirst: Boolean = false,
) {
    this.addItemDecoration(object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val childAdapterPosition = parent.getChildAdapterPosition(view)
            val layoutManager = parent.layoutManager
            when (layoutManager) {
                is GridLayoutManager -> {
                    val spanCount = layoutManager.spanCount
                    if (expectFirst && childAdapterPosition < spanCount) {
                        return
                    }
                    outRect.left = left
                    outRect.top = top
                    outRect.right = right
                    outRect.bottom = bottom
                }

                is LinearLayoutManager -> {
                    if (expectFirst && childAdapterPosition == 0) {
                        return
                    }
                    outRect.left = left
                    outRect.top = top
                    outRect.right = right
                    outRect.bottom = bottom
                }
            }
        }
    })
}


/**
 * 添加固定大小的边距装饰
 */
fun RecyclerView.addItemDecoration(size: Int) {
    this.addItemDecoration(object : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(size, size, size, size)
        }
    })
}
