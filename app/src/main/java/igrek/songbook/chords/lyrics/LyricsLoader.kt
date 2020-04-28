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
        chordsTransposerManager: LazyInject<ChordsTransposerManager> = appFactory.chordsTransposerManager,
        autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
        lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
        windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val chordsTransposerManager by LazyExtractor(chordsTransposerManager)
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val preferencesState by LazyExtractor(preferencesState)

    var lyricsModel: LyricsModel? = null
        private set
    private var screenW = 0
    private var paint: Paint? = null
    private var originalFileContent: String = ""

    private var restoreTransposition
        get() = preferencesState.restoreTransposition
        set(value) {
            preferencesState.restoreTransposition = value
        }

    fun load(fileContent: String, screenW: Int?, paint: Paint?, initialTransposed: Int, srcNotation: ChordsNotation) {
        chordsTransposerManager.run {
            val transposed = when {
                restoreTransposition -> initialTransposed
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

        parseAndTranspose(originalFileContent)
    }

    fun onPreviewSizeChange(screenW: Int, paint: Paint?) {
        this.screenW = screenW
        this.paint = paint
        reparse()
    }

    fun reparse() {
        parseAndTranspose(originalFileContent)
    }

    private fun parseAndTranspose(originalFileContent: String) {
        val transposedContent = chordsTransposerManager.transposeContent(originalFileContent)
        val realFontsize = windowManagerService.dp2px(lyricsThemeService.fontsize)
        val screenWRelative = screenW.toFloat() / realFontsize
        val typeface = lyricsThemeService.fontTypeface.typeface
        val displayStyle = lyricsThemeService.displayStyle

        val lyricsParser = LyricsParser()
        val parsedModel = lyricsParser.parseContent(transposedContent)

        val lyricsInflater = LyricsInflater(typeface, realFontsize)
        val infaltedModel = lyricsInflater.inflateLyrics(parsedModel)

        val lyricsWrapper = LyricsArranger(displayStyle, screenWRelative, lyricsInflater.lengthMapper)
        val wrappedModel = lyricsWrapper.arrangeModel(infaltedModel)

        lyricsModel = wrappedModel
    }

}
