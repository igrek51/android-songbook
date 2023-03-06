package igrek.songbook.info.logview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.logger.LogEntry
import igrek.songbook.layout.list.GenericListView
import igrek.songbook.util.formatTimestampTime

class LogListView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
) : GenericListView<LogEntry>(
    context, attrs, defStyleAttr,
    itemViewRes = R.layout.list_item_generic_string,
) {
    var onClickCallback: (item: LogEntry) -> Unit = {}

    constructor(context: Context) : this(context, null, android.R.attr.listViewStyle)

    constructor(context: Context, attrs: AttributeSet) : this(
        context,
        attrs,
        android.R.attr.listViewStyle
    )

    @SuppressLint("SetTextI18n")
    override fun buildView(view: View, item: LogEntry) {
        val timeFormatted = formatTimestampTime(item.timestampS)
        view.findViewById<TextView>(R.id.itemLabel)?.text = "[$timeFormatted] ${item.message}"
    }

    override fun onClick(item: LogEntry) {
        onClickCallback(item)
    }
}

