package com.aking.featurekit.reactive

/**
 * 用于比较最新值和上次值的差异，只有在值发生变化时才执行指定操作。
 *
 * 这个类提供了一种机制，用于缓存先前的值，并在最新值与缓存值不同的情况下触发相应的渲染操作。
 * 适用于需要响应状态变化，但仅在状态发生实际变化时执行某些操作的场景。
 *
 * @param initState 初始状态值，用于初始化该类的状态。
 *
 * @author Ak
 * @since 2025/1/10 9:43
 * @see invoke
 */
class StateDiff<S>(initState: S) {
    var newest: S = initState
        internal set
    var cache: S? = null
        internal set

    /**
     * 比较最新值和缓存值的差异，若值发生变化，则执行指定的操作。
     *
     * 该操作使用 `key` 函数从状态中提取一个关键值进行比较，若关键值发生变化，则执行 `render` 函数渲染新值。
     * 如果最新值和缓存值相同，则不会执行任何操作，避免不必要的渲染。
     *
     * @param key 从最新状态值中提取用于比较的关键值。
     * @param render 当值发生变化时，执行该函数进行渲染。
     */
    inline operator fun <T> invoke(key: (S) -> T, render: (value: T) -> Unit) {
        val value = key(newest)
        val cache = cache
        cache ?: return render(value)

        val cacheValue = key(cache)
        if (value === cacheValue) return
        if (value == cacheValue) return
        render(value)
    }

    internal inline fun update(each: S, block: () -> Unit) {
        this.newest = each
        block()
        cache = each
    }
}