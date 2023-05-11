@file:OptIn(DelicateCoroutinesApi::class)

package igrek.songbook.songpreview.autoscroll

import android.annotation.SuppressLint
import igrek.songbook.R
import igrek.songbook.cast.CastScroll
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.songpreview.renderer.SongPreview
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.DelicateCoroutinesApi
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

    private fun getVisibleScroll(): CastScroll? {
        val songPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return null
        val lyricsLoader = appFactory.lyricsLoader.g
        val lyricsModel = songPreview.lyricsModel

        val linesStartIndex: Int = floor(songPreview.firstVisibleLine).roundToInt()
        val linesEndIndex: Int = floor(songPreview.lastVisibleLine).roundToInt()

        val visualLines: List<LyricsLine> = lyricsModel.lines.filterIndexed { index, lyricsLine ->
            index in linesStartIndex..linesEndIndex
        }

        val primalStartIndex: Int = visualLines.minOf { it.primalIndex }
        val primalEndIndex: Int = visualLines.maxOf { it.primalIndex }

        val primalLines = lyricsLoader.originalLyrics.lines.filterIndexed { index, lyricsLine ->
            index in primalStartIndex..primalEndIndex
        }

        val visibleText = primalLines.joinToString("\n") {
            it.displayString()
        }

        return CastScroll(
            view_start = primalStartIndex.toFloat(),
            view_end = primalEndIndex.toFloat(),
            visible_text = visibleText,
        )
    }

    private fun onPartiallyScrolled(scrolledByLines: Float) {
        if (songCastService.isPresenting()) {
            GlobalScope.launch {
                val payload = getVisibleScroll() ?: return@launch
                val result = songCastService.postScrollControlAsync(payload).await()
                result.fold(onSuccess = {
                }, onFailure = { e ->
                    UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
                })
            }
        }
    }

    fun controlScrollFocus(viewStart: Float, viewEnd: Float, visibleText: String?) {
        val songPreview: SongPreview = appFactory.songPreviewLayoutController.g.songPreview ?: return
        val lyricsModel = songPreview.lyricsModel

        val viewCenterPrimalIndex: Float = (viewStart + viewEnd) / 2
        val centerLine = lyricsModel.lines.minBy { abs(it.primalIndex - viewCenterPrimalIndex) }
        val centerLineIndex = lyricsModel.lines.indexOf(centerLine)
        val centerLinePx = centerLineIndex * songPreview.lineheightPx
        var targetScroll = centerLinePx - songPreview.h / 2
        if (targetScroll < 0) targetScroll = 0f
        if (targetScroll > songPreview.maxScroll) targetScroll = songPreview.maxScroll

        val scrollDiff = targetScroll - songPreview.scroll
        songPreview.scrollByLines(scrollDiff)
    }

}
