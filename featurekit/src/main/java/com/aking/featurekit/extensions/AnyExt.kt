package com.aking.featurekit.extensions

/**
 * Created by Rick on 2023-11-16  17:04.
 *
 * Description: Any类扩展，提供常用的Tag生成和对象操作。
 */

/**
 * 简单类名，用于日志打印
 */
val Any.TAG: String get() = this::class.java.simpleName

/**
 * 带哈希码的类名
 * 格式：类名:哈希码，用于区分同一类的不同实例
 */
val Any.TAGHash: String get() = "${this::class.java.simpleName}:${Integer.toHexString(hashCode())}"

/**
 * 完整类名，包含包名
 * 用于需要区分不同包下同名类的场景
 */
val Any.TAGFull: String get() = this::class.java.name


/**
 * 将当前对象强制转换为指定类型T
 */
inline fun <reified T> Any.saveAs(): T {
    return this as T
}
