package igrek.songbook.info.logview

import android.annotation.SuppressLint
import android.view.View
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LogEntry
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.system.ClipboardManager

@SuppressLint("CheckResult")
class LogsLayoutController(
    clipboardManager: LazyInject<ClipboardManager> = appFactory.clipboardManager,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) : InflatedLayout(
    _layoutResourceId = R.layout.screen_logs
) {
    private val clipboardManager by LazyExtractor(clipboardManager)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private var itemsListView: LogListView? = null

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        itemsListView = layout.findViewById<LogListView>(R.id.itemsListView)?.also {
            it.init()
            it.enableNestedScrolling()
            it.onClickCallback = { item: LogEntry ->
                copyItemToClipboard(item)
            }
            it.items = listOf()
        }

        itemsListView?.let {
            populateItems(it)
        }
    }

    private fun populateItems(listView: LogListView) {
        listView.items = LoggerFactory.sessionLogs.reversed()
    }

    private fun copyItemToClipboard(item: LogEntry) {
        clipboardManager.copyToSystemClipboard(item.message)
        uiInfoService.showInfo(R.string.copied_to_clipboard)
    }
}
