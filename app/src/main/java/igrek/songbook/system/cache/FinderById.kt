package igrek.songbook.system.cache

class FinderById<T>(
        entitiesList: List<T>,
        entityToId: (T) -> Long
) : FinderByTuple<Long, T>(entitiesList, entityToId)