package igrek.songbook.chordsv2.loader

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose

class ChordsTransposerManager(
    lyricsLoader: LazyInject<LyricsLoader> = appFactory.lyricsLoader,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    quickMenuTranspose: LazyInject<QuickMenuTranspose> = appFactory.quickMenuTranspose,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    private val lyricsLoader by LazyExtractor(lyricsLoader)
    private val songPreviewController by LazyExtractor(songPreviewLayoutController)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val userInfo by LazyExtractor(uiInfoService)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val songsRepository by LazyExtractor(songsRepository)

    var transposedBy = 0
    val isTransposed: Boolean = transposedBy != 0

    val transposedByDisplayName: String = when {
        transposedBy > 0 ->"+$transposedBy ${getSemitonesDisplayName(transposedBy)}"
        else -> "$transposedBy ${getSemitonesDisplayName(transposedBy)}"
    }

    fun reset(initialTransposed: Int = 0) {
        transposedBy = initialTransposed
    }

    fun onTransposeEvent(semitones: Int) {
        transposeBy(semitones)

        songPreviewController.onLyricsModelUpdated()

        if (isTransposed) {
            userInfo.showInfoAction(
                R.string.transposed_by_semitones, transposedByDisplayName,
                actionResId = R.string.action_transposition_reset
            ) {
                this.onTransposeResetEvent()
            }
        } else {
            userInfo.showInfo(R.string.transposed_by_semitones, transposedByDisplayName)
        }

        quickMenuTranspose.onTransposedEvent()

        val song = songPreviewController.currentSong
        if (song != null)
            songsRepository.transposeDao.setSongTransposition(song.songIdentifier(), transposedBy)
    }

    fun onTransposeResetEvent() {
        transposeBy(-transposedBy)
        onTransposeEvent(-transposedBy)
    }

    private fun transposeBy(semitones: Int) {
        transposedBy += semitones
        if (transposedBy >= 12)
            transposedBy -= 12
        if (transposedBy <= -12)
            transposedBy += 12

        lyricsLoader.onTransposed()
    }

    private fun getSemitonesDisplayName(transposed: Int): String {
        val absTransposed = if (transposed >= 0) transposed else -transposed
        val stringResId: Int = when {
            absTransposed == 0 -> R.string.transpose_0_semitones
            absTransposed == 1 -> R.string.transpose_1_semitones
            absTransposed <= 4 -> // 2,3,4
                R.string.transpose_234_semitones
            else -> R.string.transpose_5_semitones
        }
        return uiResourceService.resString(stringResId)
    }

}