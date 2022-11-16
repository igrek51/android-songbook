package igrek.songbook.util.lookup

class FinderById<T>(
    entitiesList: List<T>,
    entityToId: (T) -> Long
) : FinderByTuple<Long, T>(entitiesList, entityToId)