@file:OptIn(DelicateCoroutinesApi::class)

package igrek.songbook.songpreview.autoscroll

import android.annotation.SuppressLint
import igrek.songbook.R
import igrek.songbook.cast.CastFocusControl
import igrek.songbook.cast.CastScroll
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.util.applyMin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
            .throttleLast(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onPartiallyScrolled(it)
                scrolledBuffer = 0f
            }, UiErrorHandler::handleError)
    }

    private fun getVisibleShareScroll(): CastScroll? {
        val songPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return null
        val lyricsLoader = appFactory.lyricsLoader.g
        val lyricsModel = songPreview.lyricsModel

        val firstVisibleLine: Float = songPreview.firstVisibleLine.applyMin(0f)
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
        )
    }
    private fun getVisibleSlidesScroll(visualLinesCount: Int): CastScroll? {
        // TODO

        return CastScroll(
            view_start = 0f,
            view_end = 0f,
            visible_text = "",
        )
    }

    private fun onPartiallyScrolled(scrolledByLines: Float) {
        if (songCastService.isPresenting() && songCastService.presenterFocusControl != CastFocusControl.NONE) {
            shareScrollControl()
        }
    }

    private fun shareScrollControl() {
        GlobalScope.launch {
            val payload = when (songCastService.presenterFocusControl) {
                CastFocusControl.SHARE_SCROLL -> getVisibleShareScroll()
                CastFocusControl.SLIDES_1 -> getVisibleSlidesScroll(1)
                CastFocusControl.SLIDES_2 -> getVisibleSlidesScroll(2)
                CastFocusControl.SLIDES_4 -> getVisibleSlidesScroll(4)
                else -> null
            } ?: return@launch
            logger.debug("Sharing scroll control: ${payload.view_start}")
            val result = songCastService.postScrollControlAsync(payload).await()
            result.fold(onSuccess = {
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    fun adaptToScrollControl(viewStart: Float, viewEnd: Float, visibleText: String?) {
        val songPreview: SongPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return
        val lyricsModel = songPreview.lyricsModel

        val lineStartFraction: Float = viewStart - floor(viewStart)
        val startLine = lyricsModel.lines.minBy { abs(it.primalIndex - viewStart) }
        val startLineIndex = lyricsModel.lines.indexOf(startLine)
        var targetLineScroll = startLineIndex + lineStartFraction
        if (targetLineScroll < 0) targetLineScroll = 0f
        if (targetLineScroll > songPreview.maxScroll) targetLineScroll = songPreview.maxScroll

        val scrollDiff = targetLineScroll - songPreview.scroll
        if (abs(scrollDiff) <= 0.01f) return
        GlobalScope.launch(Dispatchers.Main) {
            songPreview.scrollByLines(scrollDiff)
        }
    }

}
