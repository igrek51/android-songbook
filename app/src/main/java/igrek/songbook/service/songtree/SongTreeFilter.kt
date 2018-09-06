package igrek.songbook.service.songtree

import java.util.*

class SongTreeFilter(private val nameFilter: String?) {

    private val locale = Locale("pl", "PL")
    private val specialCharsMapping = mutableMapOf<Char, Char>()

    init {
        // special polish letters transformation
        specialCharsMapping['ą'] = 'a'
        specialCharsMapping['ż'] = 'z'
        specialCharsMapping['ś'] = 's'
        specialCharsMapping['ź'] = 'z'
        specialCharsMapping['ę'] = 'e'
        specialCharsMapping['ć'] = 'c'
        specialCharsMapping['ń'] = 'n'
        specialCharsMapping['ó'] = 'o'
        specialCharsMapping['ł'] = 'l'
    }

    fun matchesNameFilter(songItem: SongTreeItem): Boolean {
        // no filter set
        if (nameFilter == null || nameFilter.isEmpty())
            return true

        val fullName: String = songItem.song.category.displayName + " - " + songItem.song.title
        // must contain every part
        return containsEveryFilterPart(fullName, nameFilter)
    }

    private fun containsEveryFilterPart(input: String, partsFilter: String): Boolean {
        val input2 = toSimplifiedString(input)
        return partsFilter.split(" ")
                .all { part -> input2.contains(toSimplifiedString(part)) }
    }

    private fun toSimplifiedString(s: String): String {
        var s2 = s.toLowerCase(locale);
        // special chars mapping
        specialCharsMapping.forEach { k, v -> s2 = s2.replace(k, v) }
        return s2
    }
}
