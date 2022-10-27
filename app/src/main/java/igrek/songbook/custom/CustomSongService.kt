package igrek.songbook.custom

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.persistence.user.custom.CustomSongMapper
import igrek.songbook.settings.chordsnotation.ChordsNotation
import java.util.*

class CustomSongService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    editSongLayoutController: LazyInject<EditSongLayoutController> = appFactory.editSongLayoutController,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.songExportFileChooser,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val layoutController by LazyExtractor(layoutController)
    private val editSongLayoutController by LazyExtractor(editSongLayoutController)
    private val songExportFileChooser by LazyExtractor(exportFileChooser)

    fun showAddSongScreen() {
        editSongLayoutController.setCurrentSong(null)
        layoutController.showLayout(EditSongLayoutController::class)
    }

    fun showEditSongScreen(song: Song) {
        editSongLayoutController.setCurrentSong(song)
        layoutController.showLayout(EditSongLayoutController::class)
    }

    fun exportSong(song: Song) {
        var songTitle = song.title
        songTitle = songTitle.takeIf { it.lowercase().endsWith(".txt") } ?: "$songTitle.txt"
        val songContent = song.content.orEmpty()
        songExportFileChooser.showFileChooser(songContent, songTitle) {
            uiInfoService.showInfo(R.string.song_content_exported)
        }
    }

    fun addCustomSong(title: String, customCategoryName: String?, content: String, chordsNotation: ChordsNotation): Song {
        val now: Long = Date().time
        val customSong = CustomSong(
                id = 0,
                title = title,
                categoryName = customCategoryName,
                content = content,
                versionNumber = 1,
                createTime = now,
                updateTime = now,
                chordsNotation = chordsNotation
        )
        songsRepository.customSongsDao.saveCustomSong(customSong)

        val customCategory = songsRepository.customSongsRepo.allCustomCategory
        val customSongMapper = CustomSongMapper()
        val song = customSongMapper.customSongToSong(customSong)
        song.categories = mutableListOf(customCategory)
        return song
    }

    fun updateSong(song: Song, songTitle: String, customCategoryName: String?, songContent: String?, chordsNotation: ChordsNotation) {
        song.title = songTitle
        song.content = songContent
        song.customCategoryName = customCategoryName
        song.updateTime = Date().time
        song.chordsNotation = chordsNotation

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

        val newModelSong = songsRepository.customSongsDao.saveCustomSong(newSong)

        uiInfoService.showInfoAction(R.string.song_copied_as_custom,
                actionResId = R.string.song_copied_edit_it) {
            showEditSongScreen(newModelSong)
        }
        return newSong
    }
}
