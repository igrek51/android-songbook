package igrek.songbook.chords.loader

import android.graphics.Typeface
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
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.system.WindowManagerService

// Singleton
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
    private var originalSongNotation: ChordsNotation = ChordsNotation.default
    var originalLyrics: LyricsModel = LyricsModel()
        private set
    var transposedLyrics: LyricsModel = LyricsModel()
        private set
    var arrangedLyrics: LyricsModel = LyricsModel()
        private set
    var songKey: MajorKey = MajorKey.C_MAJOR
        private set

    fun load(
        fileContent: String,
        screenW: Int?,
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

        originalSongNotation = srcNotation

        originalLyrics = if (fileContent.isEmpty()) {
            LyricsModel()
        } else {
            val lyricsExtractor = LyricsExtractor(trimWhitespaces = preferencesState.trimWhitespaces)
            val lyrics = lyricsExtractor.parseLyrics(fileContent)
            val unknownChords = ChordParser(srcNotation).parseAndFillChords(lyrics)
            unknownChords.takeIf { it.isNotEmpty() }?.let {
                logger.warn("Unknown chords: ${unknownChords.joinToString(", ")}")
            }
            lyrics
        }

        transposeAndFormatLyrics()
    }

    private fun transposeAndFormatLyrics() {
        val lyrics = ChordsTransposer().transposeLyrics(originalLyrics, chordsTransposerManager.transposedBy)
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

    fun onPreviewSizeChange(screenW: Int) {
        this.screenW = screenW
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

    fun loadEphemeralLyrics(
        content: String,
        screenW: Int,
        srcNotation: ChordsNotation,
    ): LyricsModel {
        val trimWhitespaces: Boolean = preferencesState.trimWhitespaces
        val toNotation: ChordsNotation = preferencesState.chordsNotation
        val forceSharpNotes: Boolean = preferencesState.forceSharpNotes
        val fontsize: Float = lyricsThemeService.fontsize
        val typeface: Typeface = lyricsThemeService.fontTypeface.typeface
        val displayStyle: DisplayStyle = lyricsThemeService.displayStyle
        val horizontalScroll: Boolean = preferencesState.horizontalScroll

        // Extract lyrics and chords
        val lyricsExtractor = LyricsExtractor(trimWhitespaces = trimWhitespaces)
        val loadedLyrics = lyricsExtractor.parseLyrics(content)
        // Parse chords
        val unknownChords = ChordParser(srcNotation).parseAndFillChords(loadedLyrics)
        unknownChords.takeIf { it.isNotEmpty() }?.let {
            logger.warn("Unknown chords: ${unknownChords.joinToString(", ")}")
        }

        // Format chords
        val songKey = KeyDetector().detectKey(loadedLyrics)
        val originalModifiers = toNotation == originalSongNotation
        ChordsRenderer(toNotation, songKey, forceSharpNotes).formatLyrics(
            loadedLyrics,
            originalModifiers,
        )

        // Inflate text
        val realFontsize = windowManagerService.dp2px(fontsize)
        val screenWRelative = screenW.toFloat() / realFontsize
        val lyricsInflater = LyricsInflater(typeface, realFontsize)
        val inflatedLyrics = lyricsInflater.inflateLyrics(loadedLyrics)

        // Arrange lines
        val lyricsWrapper = LyricsArranger(
            displayStyle,
            screenWRelative,
            lyricsInflater.lengthMapper,
            horizontalScroll,
        )
        return lyricsWrapper.arrangeModel(inflatedLyrics)
    }

}