package igrek.songbook.songpreview.scroll

import android.annotation.SuppressLint
import igrek.songbook.R
import igrek.songbook.cast.CastScroll
import igrek.songbook.cast.CastScrollControl
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.util.applyMin
import igrek.songbook.util.defaultScope
import igrek.songbook.util.mainScope
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

@SuppressLint("CheckResult")
class ScrollService(
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
) {
    private val songCastService by LazyExtractor(songCastService)
    private val preferencesState by LazyExtractor(appFactory.preferencesState)

    private val logger: Logger = LoggerFactory.logger
    private val scrollSubject = PublishSubject.create<Float>()
    private val aggregatedScrollSubject = PublishSubject.create<Float>()
    private var scrolledBuffer = 0f

    fun reportSongScrolled(linePartScrolled: Float) {
        // monitor scroll changes\
        if (abs(linePartScrolled) > 0.01f) {
            scrollSubject.onNext(linePartScrolled)
        }
    }

    init {
        // aggreagate many little scrolls into greater parts
        scrollSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ linePartScrolled ->
                scrolledBuffer += linePartScrolled
                aggregatedScrollSubject.onNext(scrolledBuffer)
            }, UiErrorHandler::handleError)

        aggregatedScrollSubject
            .throttleLast(800, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onPartiallyScrolled()
                scrolledBuffer = 0f
            }, UiErrorHandler::handleError)
    }

    private fun onPartiallyScrolled() {
        if (songCastService.isPresenting() && preferencesState.castScrollControl != CastScrollControl.NONE) {
            shareScrollControl()
        }
    }

    fun shareScrollControl() {
        defaultScope.launch {
            val payload: CastScroll = when (preferencesState.castScrollControl) {
                CastScrollControl.SHARE_SCROLL -> getVisibleShareScroll()
                CastScrollControl.SLIDES_1 -> getVisibleSlidesScroll()
                CastScrollControl.SLIDES_2 -> getVisibleSlidesScroll()
                CastScrollControl.SLIDES_4 -> getVisibleSlidesScroll()
                CastScrollControl.SLIDES_8 -> getVisibleSlidesScroll()
                else -> null
            } ?: return@launch
            if (payload == songCastService.lastSharedScroll) return@launch
            logger.debug("Sharing scroll control: ${payload.view_start}")
            val result = songCastService.postScrollControlAsync(payload).await()
            result.fold(onSuccess = {
                songCastService.lastSharedScroll = payload
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private fun getVisibleShareScroll(): CastScroll? {
        val songPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return null
        val lyricsLoader = appFactory.lyricsLoader.g
        val lyricsModel = songPreview.lyricsModel

        val firstVisibleLine: Float = songPreview.scrollEm.applyMin(0f)
        val lastVisibleLine: Float = songPreview.lastVisibleLine.applyMin(0f)
        val linesStartIndex: Int = floor(firstVisibleLine).roundToInt()
        val linesEndIndex: Int = floor(lastVisibleLine).roundToInt()
        val linesStartFraction: Float = firstVisibleLine - linesStartIndex
        val linesEndFractoin: Float = lastVisibleLine - linesEndIndex

        val visualLines: List<LyricsLine> = lyricsModel.lines.filterIndexed { index, _ ->
            index in linesStartIndex..linesEndIndex
        }

        val primalStartIndex: Int = visualLines.minOfOrNull { it.primalIndex } ?: 0
        val primalEndIndex: Int = visualLines.maxOfOrNull { it.primalIndex } ?: 0
        val primalLines = lyricsLoader.originalLyrics.lines.filterIndexed { index, _ ->
            index in primalStartIndex..primalEndIndex
        }
        val visibleText = primalLines.joinToString("\n") {
            it.displayString()
        }

        return CastScroll(
            view_start = primalStartIndex.toFloat() + linesStartFraction,
            view_end = primalEndIndex.toFloat() + linesEndFractoin,
            visible_text = visibleText,
            mode = preferencesState.castScrollControl.id,
        )
    }

    private fun getVisibleSlidesScroll(): CastScroll? {
        val songPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return null
        val lyricsLoader = appFactory.lyricsLoader.g
        val lyricsModel: LyricsModel = songPreview.lyricsModel

        if (songPreview.castSlideMarkedLinesIndices.isEmpty()) {
            return CastScroll(
                view_start = 0f,
                view_end = 0f,
                visible_text = "",
                mode = preferencesState.castScrollControl.id,
            )
        }

        val castSlideMarkedLineTop = songPreview.castSlideMarkedLinesIndices.first()
        val castSlideMarkedLineBottom = songPreview.castSlideMarkedLinesIndices.last()
        val topLine = lyricsModel.lines.getOrNull(castSlideMarkedLineTop)
        val bottomLine = lyricsModel.lines.getOrNull(castSlideMarkedLineBottom)

        val primalIndexTop = topLine?.primalIndex ?: 0
        val primalIndexBottom = bottomLine?.primalIndex ?: 0

        val primalLines = lyricsLoader.originalLyrics.lines.filterIndexed { index, _ ->
            index in primalIndexTop..primalIndexBottom
        }
        val visibleText = primalLines.joinToString("\n") {
            it.displayString()
        }

        return CastScroll(
            view_start = primalIndexTop.toFloat(),
            view_end = primalIndexBottom.toFloat(),
            visible_text = visibleText,
            mode = preferencesState.castScrollControl.id,
        )
    }


    fun adaptToScrollControl(
        viewStart: Float,
        visibleText: String?,
        modeId: Long?,
        srcNotation: ChordsNotation
    ) {
        when (modeId) {
            CastScrollControl.SHARE_SCROLL.id -> adaptToShareScrollControl(viewStart)
            CastScrollControl.SLIDES_1.id -> adaptToSlideScrollControl(viewStart, visibleText, srcNotation)
            CastScrollControl.SLIDES_2.id -> adaptToSlideScrollControl(viewStart, visibleText, srcNotation)
            CastScrollControl.SLIDES_4.id -> adaptToSlideScrollControl(viewStart, visibleText, srcNotation)
            CastScrollControl.SLIDES_8.id -> adaptToSlideScrollControl(viewStart, visibleText, srcNotation)
            else -> return
        }
    }

    private fun adaptToShareScrollControl(viewStart: Float) {
        val songPreview: SongPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return
        val lyricsModel = songPreview.lyricsModel

        val viewStartFloor = floor(viewStart)
        val lineStartFraction: Float = viewStart - viewStartFloor
        val startLineIndex = lyricsModel.lines
            .indexOfFirst { it.primalIndex >= viewStartFloor }
            .takeIf { it >= 0 } ?: return
        var targetLineScroll = startLineIndex + lineStartFraction
        if (targetLineScroll < 0) targetLineScroll = 0f

        val scrollDiff = targetLineScroll - songPreview.scrollEm
        if (abs(scrollDiff) <= 0.01f) return
        val scrollByPx = scrollDiff * songPreview.lineheightPx
        logger.debug("scrolling by $scrollDiff lines, first line index: $startLineIndex")
        mainScope.launch {
            songPreview.overlayScrollView?.smoothScrollBy(0, scrollByPx.toInt())
        }
    }

    private fun adaptToSlideScrollControl(viewStart: Float, visibleText: String?, srcNotation: ChordsNotation) {
        val slideIndex = viewStart.toInt()
        val songPreview: SongPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return
        songPreview.showSlide(slideIndex, visibleText ?: "", srcNotation)
    }

}
