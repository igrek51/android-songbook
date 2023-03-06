package igrek.songbook.info.logview

import android.annotation.SuppressLint
import android.view.View
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout

@SuppressLint("CheckResult")
class LogsLayoutController(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_logs
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var itemsListView: LogListView? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById<LogListView>(R.id.itemsListView)?.also {
            it.onClickCallback = {}
            it.items = listOf()
            it.emptyView = layout.findViewById(R.id.emptyListTextView)
        }

        itemsListView?.let { it
            populateItems(it)
        }

    }

    private fun populateItems(listView: LogListView) {
        listView.items = LoggerFactory.sessionLogs.reversed()
    }
}
