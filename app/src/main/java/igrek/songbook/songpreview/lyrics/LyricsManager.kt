package igrek.songbook.songpreview.lyrics

import android.graphics.Paint
import dagger.Lazy
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.system.WindowManagerService
import javax.inject.Inject

class LyricsManager {

    @Inject
    lateinit var chordsTransposerManager: Lazy<ChordsTransposerManager>
    @Inject
    lateinit var autoscrollService: Lazy<AutoscrollService>
    @Inject
    lateinit var lyricsThemeService: LyricsThemeService
    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var preferencesState: PreferencesState

    private var lyricsParser: LyricsParser? = null
    var crdModel: LyricsModel? = null
        private set
    private var screenW = 0
    private var paint: Paint? = null
    private var originalFileContent: String? = null

    private var restoreTransposition
        get() = preferencesState.restoreTransposition
        set(value) {
            preferencesState.restoreTransposition = value
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    private fun normalizeContent(content: String): String {
        return content
                .replace("\r", "")
                .replace("\t", " ")
                .replace("\u00A0", " ") // NO-BREAK SPACE (0xC2 0xA0)
    }

    fun load(fileContent: String, screenW: Int?, paint: Paint?, initialTransposed: Int, srcNotation: ChordsNotation) {
        chordsTransposerManager.get().run {
            val transposed = when {
                restoreTransposition -> initialTransposed
                else -> 0
            }
            reset(transposed, srcNotation)
        }
        autoscrollService.get().reset()

        originalFileContent = normalizeContent(fileContent)

        if (screenW != null)
            this.screenW = screenW
        if (paint != null)
            this.paint = paint

        val typeface = lyricsThemeService.fontTypeface.typeface
        val chordsEndOfLine = lyricsThemeService.chordsEndOfLine
        val chordsAbove = lyricsThemeService.chordsAbove
        lyricsParser = LyricsParser(typeface, chordsEndOfLine, chordsAbove)

        parseAndTranspose(originalFileContent!!)
    }

    fun onPreviewSizeChange(screenW: Int, paint: Paint?) {
        this.screenW = screenW
        this.paint = paint
        reparse()
    }

    fun reparse() {
        parseAndTranspose(originalFileContent!!)
    }

    private fun parseAndTranspose(originalFileContent: String) {
        val transposedContent = chordsTransposerManager.get()
                .transposeContent(originalFileContent)
        val realFontsize = windowManagerService.dp2px(lyricsThemeService.fontsize)
        crdModel = lyricsParser?.parseFileContent(transposedContent, screenW.toFloat(), realFontsize, paint!!)
    }

}
