package igrek.songbook.chords.lyrics

import android.graphics.Paint
import dagger.Lazy
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.system.WindowManagerService
import javax.inject.Inject

class LyricsLoader {

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

    init {
        DaggerIoc.factoryComponent.inject(this)
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
        val transposedContent = chordsTransposerManager.get()
                .transposeContent(originalFileContent)
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
