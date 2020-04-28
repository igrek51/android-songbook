package igrek.songbook.contact

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository

class PublishSongService(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        publishSongLayoutController: LazyInject<PublishSongLayoutController> = appFactory.publishSongLayoutController,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val publishSongLayoutController by LazyExtractor(publishSongLayoutController)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun publishSong(song: Song) {
        val originalSongId = song.originalSongId
        if (originalSongId != null) {
            val identifier = SongIdentifier(originalSongId, SongNamespace.Public)
            val originalSong = songsRepository.allSongsRepo.songFinder.find(identifier)
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

        layoutController.showLayout(PublishSongLayoutController::class)
        publishSongLayoutController.prepareFields(song)
    }

}