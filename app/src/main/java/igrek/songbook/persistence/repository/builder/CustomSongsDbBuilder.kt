package igrek.songbook.persistence.repository.builder

import igrek.songbook.info.UiResourceService
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.CustomSongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomCategory
import igrek.songbook.persistence.user.custom.CustomSongMapper
import igrek.songbook.util.lookup.FinderByTuple
import igrek.songbook.util.lookup.SimpleCache

class CustomSongsDbBuilder(private val userDataDao: UserDataDao) {

    fun buildCustom(uiResourceService: UiResourceService): CustomSongsRepository {
        val allCustomCategory = Category(
                id = CategoryType.CUSTOM.id,
                type = CategoryType.CUSTOM,
                name = null,
                custom = false,
                songs = mutableListOf()
        )
        refillCategoryDisplayName(uiResourceService, allCustomCategory)
        val (customSongs, customSongsUncategorized) = assembleCustomSongs(allCustomCategory)
        return CustomSongsRepository(
                songs = SimpleCache { customSongs },
                uncategorizedSongs = SimpleCache { customSongsUncategorized },
                allCustomCategory = allCustomCategory
        )
    }

    private fun refillCategoryDisplayName(uiResourceService: UiResourceService, category: Category) {
        category.displayName = when {
            category.type.localeStringId != null ->
                uiResourceService.resString(category.type.localeStringId)
            else -> category.name
        }
    }

    private fun assembleCustomSongs(customGeneralCategory: Category): Pair<List<Song>, List<Song>> {
        val customSongs = userDataDao.customSongsDao.customSongs.songs
        val mapper = CustomSongMapper()

        // bind custom categories to songs
        userDataDao.customSongsDao.customCategories = customSongs
                .asSequence()
                .map { song ->
                    song.categoryName
                }.toSet()
                .filterNotNull()
                .filter { it.isNotEmpty() }
                .map { categoryName ->
                    CustomCategory(name = categoryName)
                }.toList()
        val customCategoryFinder = FinderByTuple(userDataDao.customSongsDao.customCategories) {
            it.name
        }
        val customSongsUncategorized = mutableListOf<Song>()
        val customModelSongs = mutableListOf<Song>()

        customSongs.forEach { customSong ->
            val song = mapper.customSongToSong(customSong)

            song.categories = mutableListOf(customGeneralCategory)
            customGeneralCategory.songs.add(song)

            customModelSongs.add(song)

            val customCategory: CustomCategory? = customCategoryFinder.find(customSong.categoryName
                    ?: "")
            if (customCategory == null) {
                customSongsUncategorized.add(song)
            } else {
                customCategory.songs.add(song)
            }
        }

        return customModelSongs to customSongsUncategorized
    }

}