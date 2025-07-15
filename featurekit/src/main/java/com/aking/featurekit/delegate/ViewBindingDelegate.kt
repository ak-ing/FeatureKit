package com.aking.featurekit.delegate

/**
 * Created by AK on 2024-03-28.
 * Description: ViewBinding拓展
 */
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KProperty

/**
 * Activity ViewBinding 委托扩展
 * 用于简化 Activity 中 ViewBinding 的初始化和使用，自动调用 [Activity.setContentView]
 *
 * 特点：
 * 1. 懒加载初始化：
 *    - 仅在首次访问时创建 binding 实例
 *    - 自动调用 setContentView，无需手动处理
 * 2. 内存安全：
 *    - 使用 late init 确保内存使用效率
 *    - 保证单一实例，避免重复创建
 *
 * 使用示例：
 * ```
 * class MainActivity : AppCompatActivity() {
 *     private val binding by contentView(ActivityMainBinding::inflate)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         // binding 可以直接使用，无需手动初始化
 *         binding.textView.text = "Hello"
 *     }
 * }
 * ```
 *
 * @param inflate ViewBinding 的 inflate 方法引用
 * @return ViewBinding 委托对象
 */
fun <A : AppCompatActivity, T : ViewBinding> AppCompatActivity.contentView(
    inflate: (LayoutInflater) -> T
): ContentViewBindingDelegate<A, T> = ContentViewBindingDelegate(inflate)

/**
 * Fragment ViewBinding 委托扩展
 * 用于简化 Fragment 中 ViewBinding 的初始化和生命周期管理
 *
 * 特点：
 * 1. 自动处理生命周期：
 *    - 在 Fragment 视图销毁时自动清除引用
 *    - 防止内存泄漏，无需手动处理
 * 2. 空安全保证：
 *    - 确保只在视图创建后才能访问 binding
 *    - 防止因视图未初始化或已销毁导致的空指针异常
 * 3. 使用简单：
 *    - 通过委托属性语法实现优雅的 binding 访问
 *    - 自动完成绑定初始化和清理
 *
 * 使用示例：
 * ```
 * class MyFragment : Fragment(R.layout.fragment_my) {
 *     private val binding by binding(FragmentMyBinding::bind)
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *         // binding 可以直接使用，自动完成初始化
 *         binding.button.setOnClickListener { }
 *     }
 * }
 * ```
 *
 * @param inflate ViewBinding 的 bind 方法引用
 * @return ViewBinding 委托对象
 */
fun <F : Fragment, T : ViewBinding> Fragment.binding(
    inflate: (View) -> T
): FragmentViewBindingDelegate<F, T> = FragmentViewBindingDelegate(inflate)

class ContentViewBindingDelegate<in A : AppCompatActivity, out T : ViewBinding>(
    private val inflate: (LayoutInflater) -> T
) {

    private lateinit var binding: T

    operator fun getValue(activity: A, property: KProperty<*>): T {
        if (this::binding.isInitialized) {
            return binding
        }
        binding = inflate.invoke(activity.layoutInflater)
        activity.setContentView(binding.root)
        return binding
    }
}

class FragmentViewBindingDelegate<in F : Fragment, out T : ViewBinding>(
    private val inflate: (View) -> T
) {

    private var binding: T? = null

    operator fun getValue(fragment: F, property: KProperty<*>): T {
        binding?.let { return it }

        val view = checkNotNull(fragment.view) {
            "The view of the fragment has not been initialized or has been destroyed!"
        }

        binding = inflate.invoke(view)

        fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(Clear(fragment), false)

        return requireNotNull(binding)
    }

    inner class Clear(private val thisRef: F) : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            if (thisRef === f) {
                binding = null
                fm.unregisterFragmentLifecycleCallbacks(this)
            }
        }
    }

}