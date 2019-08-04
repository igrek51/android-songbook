package igrek.songbook.system.cache

class FinderById<T>(entitiesList: List<T>,
                    private val entityToId: (T) -> Long) {

    private val idMapping = HashMap<Long, T>()

    init {
        entitiesList.forEach { entity ->
            val id = entityToId(entity)
            idMapping[id] = entity
        }
    }

    fun find(id: Long): T? {
        return idMapping[id]
    }

}
