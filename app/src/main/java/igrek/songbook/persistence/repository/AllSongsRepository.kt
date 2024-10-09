package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.language.SongLanguage
import igrek.songbook.settings.language.isValidLanguageCode
import igrek.songbook.util.lookup.LazyFinderByTuple
import igrek.songbook.util.lookup.SimpleCache

data class AllSongsRepository(
    val publicSongsRepo: PublicSongsRepository,
    val customSongsRepo: CustomSongsRepository,
) {

    val songs: SimpleCache<List<Song>> = SimpleCache {
        publicSongsRepo.songs.get() + customSongsRepo.songs.get()
    }

    val categories: SimpleCache<List<Category>> = SimpleCache {
        publicSongsRepo.categories.get()
    }

    var publicCategories: SimpleCache<List<Category>> = SimpleCache {
        publicSongsRepo.categories.get().filter { c -> c.type != CategoryType.CUSTOM }
    }

    val songLanguages: SimpleCache<List<SongLanguage>> = SimpleCache {
        val allSongs = publicSongsRepo.songs.get() + customSongsRepo.songs.get()
        val uniqueLangCodes = allSongs.mapNotNull { s -> s.language }.distinct().sorted()
        val validCodes = uniqueLangCodes.filter { isValidLanguageCode(it) }
        validCodes.map { SongLanguage(it) }
    }

    val songFinder = LazyFinderByTuple(
        entityToId = { song -> song.songIdentifier() },
        valuesSupplier = songs
    )

    private val categoryFinder = LazyFinderByTuple(
        entityToId = { e -> e.id },
        valuesSupplier = categories
    )

    fun invalidate() {
        publicSongsRepo.invalidate()
        customSongsRepo.invalidate()

        songs.invalidate()
        categories.invalidate()
        publicCategories.invalidate()
        songLanguages.invalidate()
        songFinder.invalidate()
        categoryFinder.invalidate()
    }

}