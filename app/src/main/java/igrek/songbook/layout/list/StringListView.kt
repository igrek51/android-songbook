package igrek.songbook.layout.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import igrek.songbook.R


class StringListView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
) : GenericListView<String>(
    context, attrs, defStyleAttr,
    itemViewRes = R.layout.list_item_generic_string,
) {
    var onClickCallback: (item: String) -> Unit = {}

    constructor(context: Context) : this(context, null, android.R.attr.listViewStyle)

    constructor(context: Context, attrs: AttributeSet) : this(
        context,
        attrs,
        android.R.attr.listViewStyle,
    )

    override fun buildView(view: View, item: String) {
        view.findViewById<TextView>(R.id.itemLabel)?.text = item
    }

    override fun onClick(item: String) {
        onClickCallback(item)
    }
}
