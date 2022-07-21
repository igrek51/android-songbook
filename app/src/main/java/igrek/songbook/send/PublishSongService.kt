package igrek.songbook.send

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class PublishSongService(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        publishSongLayoutController: LazyInject<PublishSongLayoutController> = appFactory.publishSongLayoutController,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val publishSongLayoutController by LazyExtractor(publishSongLayoutController)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun publishSong(song: Song) {
        val originalSongId = song.originalSongId
        if (originalSongId != null) {
            val identifier = SongIdentifier(originalSongId, SongNamespace.Public)
            val originalSong = songsRepository.allSongsRepo.songFinder.find(identifier)
            originalSong?.run {
                val originalContent = originalSong.content
                val newContent = song.content
                if (originalContent == newContent) {
                    uiInfoService.dialog(R.string.dialog_warning, R.string.publish_song_no_change)
                    return
                }
            }
        }

        if (song.language == null) {
            song.language = SongLanguageDetector().detectLanguageCode(song.content.orEmpty())
        }

        GlobalScope.launch(Dispatchers.Main) {
            layoutController.showLayout(PublishSongLayoutController::class).join()
            publishSongLayoutController.prepareFields(song)
        }
    }

}