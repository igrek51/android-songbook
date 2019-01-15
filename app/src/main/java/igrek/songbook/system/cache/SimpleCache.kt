package igrek.songbook.system.cache

class SimpleCache<T>(private val supplier: () -> T) {

    private var cachedValue: T? = null

    fun get(): T {
        if (cachedValue == null)
            cachedValue = supplier.invoke()
        return cachedValue!!
    }

    fun invalidate() {
        cachedValue = null
    }
}
