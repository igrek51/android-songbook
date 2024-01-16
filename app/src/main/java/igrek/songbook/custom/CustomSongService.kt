package igrek.songbook.custom

import igrek.songbook.R
import igrek.songbook.editor.SongEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.persistence.user.custom.CustomSongMapper
import igrek.songbook.settings.chordsnotation.ChordsNotation
import java.util.Date

class CustomSongService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    songEditorLayoutController: LazyInject<SongEditorLayoutController> = appFactory.songEditorLayoutController,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.exportFileChooser,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val layoutController by LazyExtractor(layoutController)
    private val editSongLayoutController by LazyExtractor(songEditorLayoutController)
    private val exportFileChooser by LazyExtractor(exportFileChooser)

    fun showAddSongScreen() {
        editSongLayoutController.setCurrentSong(null)
        layoutController.showLayout(SongEditorLayoutController::class)
    }

    fun showEditSongScreen(song: Song) {
        editSongLayoutController.setCurrentSong(song)
        layoutController.showLayout(SongEditorLayoutController::class)
    }

    fun exportSong(song: Song) {
        val songContent = song.content.orEmpty()
        val notation = song.chordsNotation
        val artist = song.displayCategories()
        exportSongContent(songContent, song.title, artist, notation)
    }

    fun exportSongContent(content: String, title: String, artist: String, notation: ChordsNotation) {
        val filename = title.takeIf { it.lowercase().endsWith(".txt") } ?: "$title.txt"
        val cleanTitle = title.replace("\"", "").replace("{", "").replace("}", "")
        var exportContent = "{title: \"$cleanTitle\"}\n"
        if (artist.isNotBlank()) {
            exportContent += "{artist: \"$artist\"}\n"
        }
        exportContent += "{chords_notation: ${notation.id}}\n" + content

        exportFileChooser.showFileChooser(exportContent, filename) {
            uiInfoService.showInfo(R.string.song_exported)
        }
    }

    fun addCustomSong(
        title: String,
        customCategoryName: String?,
        content: String,
        chordsNotation: ChordsNotation,
    ): Song {
        val now: Long = Date().time
        val customSong = CustomSong(
            id = DeviceIdProvider().newUUID(),
            title = title,
            categoryName = customCategoryName,
            content = content,
            versionNumber = 1,
            createTime = now,
            updateTime = now,
            chordsNotation = chordsNotation,
        )
        songsRepository.customSongsDao.saveCustomSong(customSong)

        val customCategory = songsRepository.customSongsRepo.allCustomCategory
        val customSongMapper = CustomSongMapper()
        val song = customSongMapper.customSongToSong(customSong)
        song.categories = mutableListOf(customCategory)
        return song
    }

    fun updateSong(
        song: Song,
        songTitle: String,
        customCategoryName: String?,
        songContent: String?,
        chordsNotation: ChordsNotation,
    ) {
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
        newSong.id = DeviceIdProvider().newUUID()
        newSong.categoryName = customCategoryName
        newSong.versionNumber = versionNumber
        newSong.createTime = now
        newSong.updateTime = now
        newSong.originalSongId = sourceSong.id

        val newModelSong = songsRepository.customSongsDao.saveCustomSong(newSong)

        uiInfoService.showInfoAction(
            R.string.song_copied_as_custom,
            actionResId = R.string.song_copied_edit_it,
        ) {
            showEditSongScreen(newModelSong)
        }
        return newSong
    }
}
