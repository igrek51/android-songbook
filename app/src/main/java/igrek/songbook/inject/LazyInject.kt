package igrek.songbook.inject

interface LazyInject<T> {
    fun get(): T
}

class SingletonInject<T>(private val supplier: () -> T) : LazyInject<T> {
    private var cached: T? = null

    override fun get(): T {
        val cachedSnapshot = cached
        if (cachedSnapshot == null) {
            val notNull = supplier.invoke()
            cached = notNull
            return notNull
        }
        return cachedSnapshot
    }

    val g: T get() = get()
}

class PrototypeInject<T>(private val supplier: () -> T) : LazyInject<T> {
    override fun get(): T = supplier.invoke()
}
