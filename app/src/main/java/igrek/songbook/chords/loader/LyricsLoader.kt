package igrek.songbook.chords.loader

import android.graphics.Paint
import igrek.songbook.chords.arranger.LyricsArranger
import igrek.songbook.chords.detect.KeyDetector
import igrek.songbook.chords.model.LyricsCloner
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.chords.parser.LyricsExtractor
import igrek.songbook.chords.render.ChordsRenderer
import igrek.songbook.chords.render.LyricsInflater
import igrek.songbook.chords.syntax.MajorKey
import igrek.songbook.chords.transpose.ChordsTransposer
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.info.logger.LoggerFactory
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

    private val logger = LoggerFactory.logger
    private val chordsTransposerManager = ChordsTransposerManager()
    private var screenW = 0
    private var paint: Paint? = null
    private var originalSongNotation: ChordsNotation = ChordsNotation.default
    private var originalLyrics: LyricsModel = LyricsModel()
    var transposedLyrics: LyricsModel = LyricsModel()
        private set
    var arrangedLyrics: LyricsModel = LyricsModel()
        private set
    var songKey: MajorKey = MajorKey.C_MAJOR
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
            val lyrics =
                LyricsExtractor(trimWhitespaces = preferencesState.trimWhitespaces).parseLyrics(fileContent)
            val unknownChords = ChordParser(srcNotation).parseAndFillChords(lyrics)
            unknownChords.takeIf { it.isNotEmpty() }?.let {
                logger.warn("Unknown chords: ${unknownChords.joinToString(", ")}")
            }
            lyrics
        }

        transposeAndFormatLyrics()
    }

    private fun transposeAndFormatLyrics() {
        val lyrics =
            ChordsTransposer().transposeLyrics(originalLyrics, chordsTransposerManager.transposedBy)
        songKey = KeyDetector().detectKey(lyrics)

        val toNotation = preferencesState.chordsNotation
        val originalModifiers = when {
            toNotation == originalSongNotation && chordsTransposerManager.transposedBy == 0 -> true
            else -> false
        }
        ChordsRenderer(toNotation, songKey, preferencesState.forceSharpNotes).formatLyrics(
            lyrics,
            originalModifiers,
        )
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
        val inflatedLyrics = lyricsInflater.inflateLyrics(lyrics)

        val lyricsWrapper = LyricsArranger(
            displayStyle,
            screenWRelative,
            lyricsInflater.lengthMapper,
            preferencesState.horizontalScroll
        )
        arrangedLyrics = lyricsWrapper.arrangeModel(inflatedLyrics)
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
        transposeAndFormatLyrics()
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