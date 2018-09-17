package igrek.songbook.songlist.tree

class SongTreeFilter(private val nameFilter: String?) {

    fun matchesNameFilter(songItem: SongTreeItem): Boolean {
        // no filter set
        if (nameFilter == null || nameFilter.isEmpty())
            return true

        val fullName: String = songItem.song.category.displayName + " - " + songItem.song.title
        // must contain every part
        return containsEveryFilterPart(fullName, nameFilter)
    }

    private fun containsEveryFilterPart(input: String, partsFilter: String): Boolean {
        val input2 = StringSimplifier.simplify(input)
        return partsFilter.split(" ")
                .all { part -> input2.contains(StringSimplifier.simplify(part)) }
    }

}
