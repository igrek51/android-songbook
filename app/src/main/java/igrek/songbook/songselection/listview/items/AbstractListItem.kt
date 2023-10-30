package igrek.songbook.songselection.listview.items

abstract class AbstractListItem {

    abstract fun simpleName(): String

    open fun id(): String? {
        return null
    }
}