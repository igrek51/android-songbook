package igrek.songbook.chordsv2.loader

import android.graphics.Paint
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.chordsv2.arranger.LyricsArranger
import igrek.songbook.chordsv2.detect.KeyDetector
import igrek.songbook.chordsv2.model.LyricsCloner
import igrek.songbook.chordsv2.render.ChordsRenderer
import igrek.songbook.chordsv2.render.LyricsInflater
import igrek.songbook.chordsv2.model.LyricsModel
import igrek.songbook.chordsv2.parser.ChordParser
import igrek.songbook.chordsv2.parser.LyricsParser
import igrek.songbook.chordsv2.transpose.ChordsTransposer
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

    private val chordsTransposerManager = ChordsTransposerManager()
    private var screenW = 0
    private var paint: Paint? = null
    private var originalSongNotation: ChordsNotation = ChordsNotation.default
    private var originalLyrics: LyricsModel = LyricsModel()
    private var transposedLyrics: LyricsModel = LyricsModel()
    var lyricsModel: LyricsModel = LyricsModel()
        private set

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
            reset(transposed)
        }
        autoscrollService.reset()

        if (screenW != null)
            this.screenW = screenW
        if (paint != null)
            this.paint = paint

        originalSongNotation = srcNotation

        originalLyrics = if (fileContent.isEmpty()) {
            LyricsModel()
        } else {
            val lyrics = LyricsParser(trimWhitespaces = preferencesState.trimWhitespaces).parseLyrics(fileContent)
            ChordParser(srcNotation).parseAndFillChords(lyrics)
            lyrics
        }

        transposeAndArrangeLyrics()
    }

    private fun transposeAndArrangeLyrics() {

        val lyrics =
            ChordsTransposer().transposeLyrics(originalLyrics, chordsTransposerManager.transposedBy)
        val songKey = KeyDetector().detectKey(lyrics)

        val toNotation = preferencesState.chordsNotation
        ChordsRenderer(toNotation, songKey).formatLyrics(lyrics)
        transposedLyrics = lyrics

        arrangeLyrics()
    }

    private fun arrangeLyrics() {
        val lyrics = LyricsCloner().cloneLyrics(transposedLyrics)

        val realFontsize = windowManagerService.dp2px(lyricsThemeService.fontsize)
        val screenWRelative = screenW.toFloat() / realFontsize
        val typeface = lyricsThemeService.fontTypeface.typeface
        val displayStyle = lyricsThemeService.displayStyle

        val lyricsInflater = LyricsInflater(typeface, realFontsize)
        val infaltedModel = lyricsInflater.inflateLyrics(lyrics)

        val lyricsWrapper = LyricsArranger(
            displayStyle,
            screenWRelative,
            lyricsInflater.lengthMapper,
            preferencesState.horizontalScroll
        )
        lyricsModel = lyricsWrapper.arrangeModel(infaltedModel)
    }

    fun onPreviewSizeChange(screenW: Int, paint: Paint?) {
        this.screenW = screenW
        this.paint = paint
        arrangeLyrics()
    }

    fun onFontSizeChanged() {
        arrangeLyrics()
    }

    fun onTransposed() {
        transposeAndArrangeLyrics()
    }

    val isTransposed: Boolean get() = chordsTransposerManager.isTransposed
    val transposedByDisplayName: String get() = chordsTransposerManager.transposedByDisplayName

    fun onTransposeEvent(semitones: Int) {
        chordsTransposerManager.onTransposeEvent(semitones)
    }

    fun onTransposeResetEvent() {
        chordsTransposerManager.onTransposeResetEvent()
    }


}