package igrek.songbook.service.songtree

import java.text.Collator
import java.util.*

class SongTreeFilter(val nameFilter: String?) {

    private val locale = Locale("pl", "PL")
    private val stringCollator = Collator.getInstance(locale)

    fun matchesNameFilter(songItem: SongTreeItem): Boolean {
        // no filter set
        if (nameFilter == null || nameFilter.isEmpty())
            return true

        val fullName: String = songItem.song.category.displayName + " - " + songItem.song.title
        // must contain every part
        return containsEveryFilterPart(fullName, nameFilter)
    }

    private fun containsEveryFilterPart(input: String, partsFilter: String): Boolean {
        val input2 = input.toLowerCase(locale)
        return partsFilter.split(" ")
                .all { part -> input2.contains(part.toLowerCase(locale)) }
    }
}
