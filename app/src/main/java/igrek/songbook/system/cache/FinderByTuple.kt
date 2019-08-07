package igrek.songbook.system.cache

open class FinderByTuple<K, V>(entitiesList: List<V>,
                       private val entityToId: (V) -> K) {

    private val idMapping = hashMapOf<K, V>()

    init {
        entitiesList.forEach { entity ->
            val id = entityToId(entity)
            idMapping[id] = entity
        }
    }

    fun find(id: K): V? {
        return idMapping[id]
    }

}
