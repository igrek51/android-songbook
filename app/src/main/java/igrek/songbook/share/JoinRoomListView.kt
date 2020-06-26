package igrek.songbook.share

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.layout.list.GenericListView

class JoinRoomListView(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.listViewStyle,
) : GenericListView<JoinRoom>(
        context, attrs, defStyleAttr,
        itemViewRes = R.layout.list_item_generic_string,
) {

    var onClickCallback: (item: JoinRoom) -> Unit = {}

    constructor(context: Context) : this(context, null, android.R.attr.listViewStyle)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, android.R.attr.listViewStyle)

    override fun buildView(view: View, item: JoinRoom) {
        view.findViewById<TextView>(R.id.itemLabel)?.text = item.name
    }

    override fun onClick(item: JoinRoom) {
        onClickCallback(item)
    }
}
