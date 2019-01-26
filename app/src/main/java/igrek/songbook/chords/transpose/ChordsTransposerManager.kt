package igrek.songbook.chords.transpose

import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.lyrics.LyricsManager
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import javax.inject.Inject

class ChordsTransposerManager {

    private var transposedBy = 0
    private var chordsTransposer: ChordsTransposer? = null

    @Inject
    lateinit var lyricsManager: Lazy<LyricsManager>
    @Inject
    lateinit var chordsNotationService: Lazy<ChordsNotationService>
    @Inject
    lateinit var songPreviewController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var userInfo: UiInfoService
    @Inject
    lateinit var quickMenuTranspose: Lazy<QuickMenuTranspose>

    val isTransposed: Boolean
        get() = transposedBy != 0

    val transposedByDisplayName: String
        get() = (if (transposedBy > 0) "+" else "") + transposedBy + " " + getSemitonesDisplayName(transposedBy)

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun reset() {
        transposedBy = 0
        val chordsNotation = chordsNotationService.get().chordsNotation
        chordsTransposer = ChordsTransposer(chordsNotation!!)
    }

    fun transposeContent(fileContent: String): String {
        return chordsTransposer!!.transposeContent(fileContent, transposedBy)
    }

    fun onTransposeEvent(semitones: Int) {
        transposeBy(semitones)

        songPreviewController.get().onLyricsModelUpdated()

        val info = uiResourceService.resString(R.string.transposed_by_semitones, transposedByDisplayName)

        if (isTransposed) {
            userInfo.showInfoWithAction(info, R.string.action_transposition_reset) { this.onTransposeResetEvent() }
        } else {
            userInfo.showInfo(info)
        }

        quickMenuTranspose.get().onTransposedEvent()
    }

    fun onTransposeResetEvent() {
        onTransposeEvent(-transposedBy)
    }

    private fun transposeBy(semitones: Int) {
        transposedBy += semitones
        if (transposedBy >= 12)
            transposedBy -= 12
        if (transposedBy <= -12)
            transposedBy += 12
        lyricsManager.get().reparse()
    }

    private fun getSemitonesDisplayName(transposed: Int): String {
        val absTransposed = if (transposed >= 0) transposed else -transposed
        val stringResId: Int
        stringResId = when {
            absTransposed == 0 -> R.string.transpose_0_semitones
            absTransposed == 1 -> R.string.transpose_1_semitones
            absTransposed <= 4 -> // 2,3,4
                R.string.transpose_234_semitones
            else -> R.string.transpose_5_semitones
        }
        return uiResourceService.resString(stringResId)
    }

}
