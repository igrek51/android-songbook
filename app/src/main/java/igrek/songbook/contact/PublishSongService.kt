package igrek.songbook.contact

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import javax.inject.Inject

class PublishSongService {

    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var publishSongLayoutController: dagger.Lazy<PublishSongLayoutController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun publishSong(song: Song) {
        val originalSongId = song.originalSongId
        if (originalSongId != null) {
            val identifier = SongIdentifier(originalSongId, SongNamespace.Public)
            val originalSong = songsRepository.songsDb?.songFinder?.find(identifier)
            originalSong?.run {
                val originalContent = originalSong.content
                val newContent = song.content
                if (originalContent == newContent) {
                    val title = uiResourceService.resString(R.string.dialog_warning)
                    val message = uiResourceService.resString(R.string.publish_song_no_change)
                    uiInfoService.showDialog(title, message)
                    return
                }
            }
        }

        layoutController.get().showLayout(PublishSongLayoutController::class)
        publishSongLayoutController.get().prepareFields(song.title, song.customCategoryName, song.content, originalSongId)
    }

}