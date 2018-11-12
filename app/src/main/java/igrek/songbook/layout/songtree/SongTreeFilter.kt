package igrek.songbook.layout.songtree

import igrek.songbook.layout.songselection.SongTreeItem
import igrek.songbook.system.locale.StringSimplifier

class SongTreeFilter(private val nameFilter: String?) {

    fun songMatchesNameFilter(songItem: SongTreeItem): Boolean {
        // no filter set
        if (nameFilter == null || nameFilter.isEmpty())
            return true

        val fullName: String = songItem.song.category.displayName + songItem.song.title
        // must contain every part
        return containsEveryFilterPart(fullName, nameFilter)
    }

    fun categoryMatchesNameFilter(songItem: SongTreeItem): Boolean {
        // no filter set
        if (nameFilter == null || nameFilter.isEmpty())
            return true

        val fullName: String = songItem.category.displayName ?: return false
        // must contain every part
        return containsEveryFilterPart(fullName, nameFilter)
    }

    private fun containsEveryFilterPart(input: String, partsFilter: String): Boolean {
        val input2 = StringSimplifier.simplify(input)
        return partsFilter.split(" ")
                .all { part -> input2.contains(StringSimplifier.simplify(part)) }
    }

}
