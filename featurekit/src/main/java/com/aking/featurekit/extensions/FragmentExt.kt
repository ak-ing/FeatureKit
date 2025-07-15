package com.aking.featurekit.extensions

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment


/**
 * @author Ak
 * 2025/6/20  10:25
 */

/**
 * 设置系统栏(如状态栏、导航栏)为临时显示模式并隐藏
 * 临时显示模式下，用户可以通过滑动手势临时显示系统栏
 *
 * @param types 要隐藏的系统栏类型，使用[WindowInsetsCompat.Type]中定义的常量
 * 例如：WindowInsetsCompat.Type.statusBars()、WindowInsetsCompat.Type.navigationBars()等
 */
fun Fragment.setSystemBarsTransientAndHide(@WindowInsetsCompat.Type.InsetsType types: Int) {
    WindowCompat.getInsetsController(requireActivity().window, requireView()).also {
        it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        it.hide(types)
    }
}


/**
 * 显示系统栏(如状态栏、导航栏)
 *
 * @param types 要显示的系统栏类型，使用[WindowInsetsCompat.Type]中定义的常量
 * 例如：WindowInsetsCompat.Type.statusBars()、WindowInsetsCompat.Type.navigationBars()等
 */
fun Fragment.showSystemBars(@WindowInsetsCompat.Type.InsetsType types: Int) {
    WindowCompat.getInsetsController(requireActivity().window, requireView()).also {
        it.show(types)
    }
}
