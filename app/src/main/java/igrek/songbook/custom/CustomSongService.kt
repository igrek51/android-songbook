package igrek.songbook.custom

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.model.songsdb.Song
import igrek.songbook.model.songsdb.SongCategoryType
import igrek.songbook.model.songsdb.SongStatus
import igrek.songbook.persistence.SongsRepository
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
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showAddSongScreen() {
        customSongEditLayoutController.get().setCurrentSong(null)
        layoutController.showEditSong()
    }

    fun showEditSongScreen(song: Song) {
        customSongEditLayoutController.get().setCurrentSong(song)
        layoutController.showEditSong()
    }

    fun addCustomSong(title: String, customCategoryName: String?, content: String?): Song {
        val versionNumber: Long = 1
        val now = Date().time
        val category = songsRepository.getCustomCategoryByTypeId(SongCategoryType.CUSTOM.id)
        val newSong = Song(0, title, category!!, content, versionNumber, now, now, true, title, null, null, false, null, null, SongStatus.PROPOSED, customCategoryName, null, null, null, null, null)
        songsRepository.addCustomSong(newSong)
        return newSong
    }

    fun updateSong(currentSong: Song, songTitle: String, customCategoryName: String?, songContent: String?) {
        currentSong.title = songTitle
        currentSong.content = songContent
        currentSong.customCategoryName = customCategoryName
        songsRepository.updateCustomSong(currentSong)
    }

    fun removeSong(currentSong: Song) {
        songsRepository.removeCustomSong(currentSong)
    }
}
