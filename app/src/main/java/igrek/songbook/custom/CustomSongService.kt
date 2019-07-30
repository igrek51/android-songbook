package igrek.songbook.custom

import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategoryType
import igrek.songbook.persistence.songsdb.SongStatus
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
    lateinit var customSongEditLayoutController: Lazy<CustomSongEditLayoutController>

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showAddSongScreen() {
        customSongEditLayoutController.get().setCurrentSong(null)
        layoutController.showCustomSong()
    }

    fun showEditSongScreen(song: Song) {
        customSongEditLayoutController.get().setCurrentSong(song)
        layoutController.showCustomSong()
    }

    fun addCustomSong(title: String, customCategoryName: String?, content: String?): Song {
        val versionNumber: Long = 1
        val now = Date().time
        val category = songsRepository.getCustomCategoryByTypeId(SongCategoryType.CUSTOM.id)
        val newSong = Song(0, title, category!!, content, versionNumber, now, now, true, title, null, null, false, null, null, SongStatus.PROPOSED, customCategoryName, null, null, null, null, null, null, null)
        songsRepository.addCustomSong(newSong)
        return newSong
    }

    fun updateSong(currentSong: Song, songTitle: String, customCategoryName: String?, songContent: String?) {
        currentSong.title = songTitle
        currentSong.content = songContent
        currentSong.customCategoryName = customCategoryName
        currentSong.updateTime = Date().time
        songsRepository.updateCustomSong(currentSong)
    }

    fun removeSong(currentSong: Song) {
        songsRepository.removeCustomSong(currentSong)
        uiInfoService.showInfo(R.string.edit_song_has_been_removed)
    }

    fun copySongAsCustom(sourceSong: Song): Song {
        val versionNumber: Long = sourceSong.versionNumber + 1
        val now = Date().time
        val category = songsRepository.getCustomCategoryByTypeId(SongCategoryType.CUSTOM.id)!!
        val customCategoryName = sourceSong.customCategoryName ?: sourceSong.category.displayName
        val newSong = Song(0, sourceSong.title, category, sourceSong.content, versionNumber, now, now, true, null, sourceSong.comment, sourceSong.preferredKey, false, null, null, SongStatus.PROPOSED, customCategoryName, sourceSong.language, sourceSong.metre, null, sourceSong.scrollSpeed, sourceSong.initialDelay)
        songsRepository.addCustomSong(newSong)
        uiInfoService.showInfo(R.string.song_copied_as_custom)
        return newSong
    }
}
