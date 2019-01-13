package igrek.songbook.songpreview

import android.graphics.Paint
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.lyrics.LyricsModel
import igrek.songbook.model.lyrics.LyricsParser
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.theme.LyricsThemeService
import igrek.songbook.songpreview.transpose.ChordsTransposerManager
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

    private var lyricsParser: LyricsParser? = null
    var crdModel: LyricsModel? = null
        private set
    private var screenW = 0
    private var paint: Paint? = null
    private var originalFileContent: String? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    private fun reset() {
        chordsTransposerManager.get().reset()
        autoscrollService.get().reset()
    }

    private fun normalizeContent(content: String): String {
        return content
                .replace("\r", "")
                .replace("\t", " ")
                .replace("\u00A0", " ") // NO-BREAK SPACE (0xC2 0xA0)
    }

    fun load(fileContent: String, screenW: Int?, screenH: Int?, paint: Paint?) {
        reset()
        originalFileContent = normalizeContent(fileContent)

        if (screenW != null)
            this.screenW = screenW
        if (paint != null)
            this.paint = paint

        val typeface = lyricsThemeService.fontTypeface!!.typeface
        lyricsParser = LyricsParser(typeface)

        parseAndTranspose(originalFileContent)
    }

    fun onPreviewSizeChange(screenW: Int, screenH: Int, paint: Paint) {
        this.screenW = screenW
        this.paint = paint
        reparse()
    }

    fun reparse() {
        parseAndTranspose(originalFileContent)
    }

    private fun parseAndTranspose(originalFileContent: String?) {
        val transposedContent = chordsTransposerManager.get()
                .transposeContent(originalFileContent)
        val realFontsize = windowManagerService.dp2px(lyricsThemeService.fontsize)
        crdModel = lyricsParser?.parseFileContent(transposedContent, screenW.toFloat(), realFontsize, paint!!)
    }

}
