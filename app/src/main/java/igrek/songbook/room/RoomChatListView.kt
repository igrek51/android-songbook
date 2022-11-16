package igrek.songbook.room

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.layout.list.GenericListView
import java.text.SimpleDateFormat
import java.util.*

class RoomChatListView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
) : GenericListView<ChatMessage>(
    context, attrs, defStyleAttr,
    itemViewRes = R.layout.list_item_generic_string,
) {

    var onClickCallback: (item: ChatMessage) -> Unit = {}
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

    constructor(context: Context) : this(context, null, android.R.attr.listViewStyle)

    constructor(context: Context, attrs: AttributeSet) : this(
        context,
        attrs,
        android.R.attr.listViewStyle
    )

    override fun buildView(view: View, item: ChatMessage) {
        val timeFormatted = dateFormat.format(item.time)
        view.findViewById<TextView>(R.id.itemLabel)?.text =
            "[$timeFormatted] ${item.author}: ${item.message}"
    }

    override fun onClick(item: ChatMessage) {
        onClickCallback(item)
    }
}

