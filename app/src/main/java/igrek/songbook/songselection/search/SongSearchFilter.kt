package igrek.songbook.songselection.search

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.system.locale.StringSimplifier

class SongSearchFilter(
    private val filterStr: String,
    private val fullTextSearch: Boolean = true,
) {

    private val simplifiedFilter: String = StringSimplifier.simplify(filterStr).trim()
    private val filterParts: List<String> = simplifiedFilter
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    fun matchSong(song: Song): Boolean {
        if (filterStr.isBlank())
            return true

        if (containsEveryFilterPart(song.displayName()))
            return true

        if (fullTextSearch && filterStr.length >= 4)
            if (containsEveryFilterPart(song.content.orEmpty()))
                return true

        return false
    }

    fun matchCategory(category: Category): Boolean {
        if (filterStr.isBlank())
            return true

        val fullName: String = category.displayName ?: return false
        return containsEveryFilterPart(fullName)
    }

    private fun containsEveryFilterPart(input: String): Boolean {
        val input2 = StringSimplifier.simplify(input)
        return filterParts.all { part -> input2.contains(part) }
    }

    fun matchSongRelevance(song: Song): Int {
        if (filterStr.isBlank())
            return 0

        if (StringSimplifier.simplify(song.title).startsWith(simplifiedFilter))
            return 1000

        if (containsEveryFilterPart(song.title))
            return 100

        if (containsEveryFilterPart(song.displayName()))
            return 10

        if (fullTextSearch && filterStr.length >= 4)
            if (containsEveryFilterPart(song.content.orEmpty()))
                return 1

        return 0
    }

}

fun List<Song>.sortSongsByFilterRelevance(filter: SongSearchFilter): List<Song> {
    val songsRelevance = this.map { song -> song to filter.matchSongRelevance(song) }
    return songsRelevance.sortedWith(
        compareBy<Pair<Song, Int>> { pair -> -pair.second }
            .thenBy { pair -> pair.first.displayName().lowercase(StringSimplifier.locale) }
    ).map { pair -> pair.first }
}
