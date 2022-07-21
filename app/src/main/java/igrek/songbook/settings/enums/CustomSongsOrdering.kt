package igrek.songbook.settings.enums

import igrek.songbook.R

enum class CustomSongsOrdering(val id: Long, val nameResId: Int) {

    SORT_BY_TITLE(1, R.string.song_sorting_by_title),
    SORT_BY_ARTIST(2, R.string.song_sorting_by_artist),
    SORT_BY_LATEST(3, R.string.song_sorting_by_latest),
    SORT_BY_OLDEST(4, R.string.song_sorting_by_oldest),
    GROUP_CATEGORIES(5, R.string.song_sorting_group_categories),
    ;

    companion object {
        val default: CustomSongsOrdering
            get() = SORT_BY_TITLE

        fun parseById(id: Long): CustomSongsOrdering? {
            return values().firstOrNull { v -> v.id == id }
        }

        fun mustParseById(id: Long): CustomSongsOrdering {
            return parseById(id) ?: default
        }
    }
}
