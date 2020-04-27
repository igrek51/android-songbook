package igrek.songbook.inject

class LazyInject<T>(private val supplier: () -> T) {
    private var cached: T? = null

    fun get(): T {
        val cachedSnapshot = cached
        if (cachedSnapshot == null) {
            val notNull = supplier.invoke()
            cached = notNull
            return notNull
        }
        return cachedSnapshot
    }
}
