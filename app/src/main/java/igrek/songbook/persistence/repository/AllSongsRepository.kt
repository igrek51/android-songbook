package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.util.lookup.LazyFinderByTuple
import igrek.songbook.util.lookup.SimpleCache

data class AllSongsRepository(
        val publicSongsRepo: PublicSongsRepository,
        val customSongsRepo: CustomSongsRepository
) {

    val songs = SimpleCache {
        publicSongsRepo.songs.get() + customSongsRepo.songs.get()
    }

    val categories = SimpleCache {
        publicSongsRepo.categories.get()
    }

    var publicCategories: SimpleCache<List<Category>> = SimpleCache {
        publicSongsRepo.categories.get().filter { c -> c.type != CategoryType.CUSTOM }
    }

    val songFinder = LazyFinderByTuple(
            entityToId = { song -> song.songIdentifier() },
            valuesSupplier = songs
    )

    val categoryFinder = LazyFinderByTuple(
            entityToId = { e -> e.id },
            valuesSupplier = categories
    )

    fun invalidate() {
        songs.invalidate()
        categories.invalidate()
        publicCategories.invalidate()
        songFinder.invalidate()
        categoryFinder.invalidate()
    }

}