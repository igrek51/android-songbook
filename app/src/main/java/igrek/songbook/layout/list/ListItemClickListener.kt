package igrek.songbook.layout.list

interface ListItemClickListener<T> {

    fun onItemClick(item: T)

    fun onItemLongClick(item: T)

    fun onMoreActions(item: T)

}
