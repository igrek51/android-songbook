package igrek.songbook.chords.lyrics

import android.graphics.Paint
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.system.WindowManagerService

class LyricsLoader(
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val preferencesState by LazyExtractor(preferencesState)

    var lyricsModel: LyricsModel = LyricsModel()
        private set
    private val chordsTransposerManager = ChordsTransposerManager()
    private var screenW = 0
    private var paint: Paint? = null
    private var originalFileContent: String = ""

    fun load(
        fileContent: String,
        screenW: Int?,
        paint: Paint?,
        initialTransposed: Int,
        srcNotation: ChordsNotation,
    ) {
        chordsTransposerManager.run {
            val transposed = when {
                preferencesState.restoreTransposition -> initialTransposed
                else -> 0
            }
            reset(transposed, srcNotation)
        }
        autoscrollService.reset()

        originalFileContent = fileContent

        if (screenW != null)
            this.screenW = screenW
        if (paint != null)
            this.paint = paint

        reparse()
    }

    fun onPreviewSizeChange(screenW: Int, paint: Paint?) {
        this.screenW = screenW
        this.paint = paint
        reparse()
    }

    fun onFontSizeChanged() {
        reparse()
    }

    fun onTransposed() {
        reparse()
    }

    val isTransposed: Boolean get() = chordsTransposerManager.isTransposed
    val transposedByDisplayName: String get() = chordsTransposerManager.transposedByDisplayName

    fun onTransposeEvent(semitones: Int) {
        chordsTransposerManager.onTransposeEvent(semitones)
    }

    fun onTransposeResetEvent() {
        chordsTransposerManager.onTransposeResetEvent()
    }

    private fun reparse() {
        if (originalFileContent.isEmpty()) {
            lyricsModel = LyricsModel()
            return
        }

        val transposedContent = chordsTransposerManager.transposeContent(originalFileContent)
        val realFontsize = windowManagerService.dp2px(lyricsThemeService.fontsize)
        val screenWRelative = screenW.toFloat() / realFontsize
        val typeface = lyricsThemeService.fontTypeface.typeface
        val displayStyle = lyricsThemeService.displayStyle

        val lyricsParser = LyricsParser(trimWhitespaces = preferencesState.trimWhitespaces)
        val parsedModel = lyricsParser.parseContent(transposedContent)

        val lyricsInflater = LyricsInflater(typeface, realFontsize)
        val infaltedModel = lyricsInflater.inflateLyrics(parsedModel)

        val lyricsWrapper = LyricsArranger(
            displayStyle,
            screenWRelative,
            lyricsInflater.lengthMapper,
            preferencesState.horizontalScroll
        )
        val wrappedModel = lyricsWrapper.arrangeModel(infaltedModel)

        lyricsModel = wrappedModel
    }

}
