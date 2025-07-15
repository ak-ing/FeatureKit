package com.aking.featurekit.util

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 壁纸设置处理接口
 */
fun interface IWallpaperSetter {
    suspend fun setWallpaper(context: Context)
}

/**
 * 接收器委托接口
 */
fun interface IReceiverDelegate {
    fun onReceiver(context: Context, intent: Intent)
}

/**
 * 壁纸设置广播接收器
 * 默认使用 wallpaper_def 资源作为壁纸
 * ```kotlin
 *     <application>
 *         <!-- 静态注册壁纸设置广播接收器 -->
 *         <receiver
 *             android:name=".util.SetWallpaperReceiver"
 *             android:exported="true">
 *             <intent-filter>
 *                 <action android:name="android.intent.LAUNCHER.LauncherChoose" />
 *             </intent-filter>
 *         </receiver>
 *     </application>
 * ```
 *
 * @author Ak
 * @since 2025/6/5
 */
open class SetWallpaperReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LAUNCHER_CHOOSE = "android.intent.LAUNCHER.LauncherChoose"
        const val EXTRA_PACKAGE_NAME_MSG = "msg"
        private const val TAG = "SetWallpaperReceiver"

        /**
         * 默认的壁纸设置实现
         */
        private object DefaultWallpaperSetter : IWallpaperSetter {
            @SuppressLint("MissingPermission")
            override suspend fun setWallpaper(context: Context) {
                context.resources.getIdentifier("wallpaper_def", "drawable", context.packageName).let { resId ->
                    WallpaperManager.getInstance(context).setResource(resId)
                }
            }
        }

        private var wallpaperSetter: IWallpaperSetter = DefaultWallpaperSetter
        private var receiverDelegate: IReceiverDelegate? = null

        /**
         * 设置自定义的壁纸设置实现
         *
         * 使用示例:
         * ```kotlin
         * // 实现自定义的壁纸设置器
         * class MyWallpaperSetter : IWallpaperSetter {
         *     override fun setWallpaper(context: Context) {
         *         // 自定义实现
         *         WallpaperManager.getInstance(context, intent: Intent)
         *             .setResource(R.drawable.my_wallpaper)
         *     }
         * }
         *
         * // 在合适的时机设置自定义实现
         * SetWallpaperReceiver.setWallpaperSetter(MyWallpaperSetter())
         * ```
         */
        @JvmStatic
        fun setWallpaperSetter(setter: IWallpaperSetter) {
            wallpaperSetter = setter
        }

        /**
         * 设置接收器委托
         *
         * 使用示例:
         * ```kotlin
         * // 实现自定义的接收器委托
         * class MyReceiverDelegate : IReceiverDelegate {
         *     override fun onReceiver(context: Context) {
         *         // 自定义处理逻辑
         *     }
         * }
         *
         * // 在合适的时机设置自定义委托
         * SetWallpaperReceiver.setReceiverDelegate(MyReceiverDelegate())
         * ```
         */
        @JvmStatic
        fun setReceiverDelegate(delegate: IReceiverDelegate) {
            receiverDelegate = delegate
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        val action = intent.action
        val packageName = intent.extras?.getString(EXTRA_PACKAGE_NAME_MSG)
        Log.i(TAG, "onReceive: action=$action, packageName=$packageName")
        if (ACTION_LAUNCHER_CHOOSE == action && packageName == context.packageName) {
            receiverDelegate?.let {
                return it.onReceiver(context, intent)
            }
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    delay(200) // 延时200毫秒，确保壁纸设置在合适的时机执行
                    wallpaperSetter.setWallpaper(context)
                }.onFailure { e -> Log.e(TAG, "Failed to set default wallpaper", e) }
            }
        }
    }
}