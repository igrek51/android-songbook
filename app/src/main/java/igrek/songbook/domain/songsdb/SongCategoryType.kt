package igrek.songbook.domain.songsdb

import igrek.songbook.R

enum class SongCategoryType(val id: Long, val localeStringId: Int?) {

    ARTIST(1, null),

    CUSTOM(2, R.string.song_category_custom),

    OTHERS(3, R.string.song_category_others);

    companion object {
        fun parseById(id: Long): SongCategoryType {
            return values().first { v -> v.id == id }
        }
    }
}
