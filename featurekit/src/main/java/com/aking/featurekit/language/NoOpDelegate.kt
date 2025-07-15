package com.aking.featurekit.language

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * 空实现委托，用于创建接口的无操作实现
 *
 * 特点：
 * 1. 选择性实现：
 *    - 无需实现接口的所有方法
 *    - 未实现的方法自动返回默认值（null或基本类型默认值）
 * 2. 类型安全：
 *    - 使用 reified 保证类型安全
 *    - 编译时类型检查
 * 3. 使用简单：
 *    - 一行代码即可创建代理实例
 *    - 适用于任意接口类型
 *
 * 使用示例：
 * ```
 * interface MyCallback {
 *     fun onSuccess(data: String)
 *     fun onError(e: Exception)
 *     fun onProgress(progress: Int)
 * }
 *
 * // 创建空实现，只关心成功回调
 * class SuccessOnlyCallback : MyCallback by noOpDelegate<MyCallback>() {
 *     override fun onSuccess(data: String) {
 *         // 只处理成功的情况
 *         println("Success: $data")
 *     }
 *     // onError 和 onProgress 会自动返回默认值，无需实现
 * }
 * ```
 *
 * @param T 要创建空实现的接口类型
 * @return 返回接口T的代理实例，所有未实现的方法都返回默认值
 * @throws IllegalArgumentException 如果T不是接口类型
 */
inline fun <reified T : Any> noOpDelegate(): T {
    val javaClass = T::class.java
    return Proxy.newProxyInstance(
        javaClass.classLoader, arrayOf(javaClass), NO_OP_HANDLER
    ) as T
}

/**
 * 空操作处理器，处理所有方法调用
 *
 * - 对未实现的方法返回 null 或对应类型的默认值
 * - 不执行任何实际操作
 */
val NO_OP_HANDLER = InvocationHandler { _, _, _ ->
    // no op
}
