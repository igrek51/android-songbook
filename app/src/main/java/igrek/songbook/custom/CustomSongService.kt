package igrek.songbook.custom

import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.persistence.user.custom.CustomSongMapper
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import java.util.*
import javax.inject.Inject

class CustomSongService {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var editSongLayoutController: Lazy<EditSongLayoutController>
    @Inject
    lateinit var preferencesService: PreferencesService

    var customSongsGroupCategories: Boolean = false

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        customSongsGroupCategories = preferencesService.getValue(PreferencesDefinition.CustomSongsGroupCategories, Boolean::class.java)
                ?: false
    }

    fun showAddSongScreen() {
        editSongLayoutController.get().setCurrentSong(null)
        layoutController.showLayout(EditSongLayoutController::class)
    }

    fun showEditSongScreen(song: Song) {
        editSongLayoutController.get().setCurrentSong(song)
        layoutController.showLayout(EditSongLayoutController::class)
    }

    fun addCustomSong(title: String, customCategoryName: String?, content: String): Song {
        val now: Long = Date().time
        val customSong = CustomSong(
                id = 0,
                title = title,
                categoryName = customCategoryName,
                content = content,
                versionNumber = 1,
                createTime = now,
                updateTime = now,
                chordsNotation = ChordsNotation.default
        )
        songsRepository.customSongsDao.saveCustomSong(customSong)

        val customCategory = songsRepository.songsDb?.categoryFinder?.find(CategoryType.CUSTOM.id)!!
        val customSongMapper = CustomSongMapper()
        val song = customSongMapper.customSongToSong(customSong)
        song.categories = mutableListOf(customCategory)
        return song
    }

    fun updateSong(song: Song, songTitle: String, customCategoryName: String?, songContent: String?) {
        song.title = songTitle
        song.content = songContent
        song.customCategoryName = customCategoryName
        song.updateTime = Date().time

        if (song.namespace == SongNamespace.Custom) {
            val customSongMapper = CustomSongMapper()
            val customSong = customSongMapper.songToCustomSong(song)
            songsRepository.customSongsDao.saveCustomSong(customSong)
        }
    }

    fun removeSong(song: Song) {
        val customSongMapper = CustomSongMapper()
        val customSong = customSongMapper.songToCustomSong(song)

        songsRepository.customSongsDao.removeCustomSong(customSong)
        uiInfoService.showInfo(R.string.edit_song_has_been_removed)
    }

    fun copySongAsCustom(sourceSong: Song): CustomSong {
        val now: Long = Date().time
        val customSongMapper = CustomSongMapper()

        val versionNumber: Long = sourceSong.versionNumber + 1
        val customCategoryName = sourceSong.customCategoryName ?: sourceSong.displayCategories()

        val newSong = customSongMapper.songToCustomSong(sourceSong)
        newSong.id = 0
        newSong.categoryName = customCategoryName
        newSong.versionNumber = versionNumber
        newSong.createTime = now
        newSong.updateTime = now
        newSong.originalSongId = sourceSong.id

        songsRepository.customSongsDao.saveCustomSong(newSong)

        uiInfoService.showInfo(R.string.song_copied_as_custom)
        return newSong
    }
}
