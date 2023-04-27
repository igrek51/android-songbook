package igrek.songbook.cast

import android.view.View
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class SongCastLayout(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_cast
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    override fun showLayout(layout: View) {
        super.showLayout(layout)
    }

}