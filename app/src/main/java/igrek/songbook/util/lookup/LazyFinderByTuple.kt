package igrek.songbook.util.lookup

open class LazyFinderByTuple<K, V>(
    private val entityToId: (V) -> K,
    private val valuesSupplier: SimpleCache<List<V>>
) {

    private val idMapping = hashMapOf<K, V>()
    private var initialized: Boolean = false

    private fun initialize() {
        val entitiesList = valuesSupplier.get()
        entitiesList.forEach { entity ->
            val id = entityToId(entity)
            idMapping[id] = entity
        }
        initialized = true
    }

    fun find(identifier: K): V? {
        if (!initialized) {
            initialize()
        }

        return idMapping[identifier]
    }

    fun invalidate() {
        initialized = false
        idMapping.clear()
        valuesSupplier.invalidate()
    }

}
