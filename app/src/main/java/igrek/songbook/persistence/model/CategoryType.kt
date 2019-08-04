package igrek.songbook.persistence.model

import igrek.songbook.R

enum class CategoryType(val id: Long, val localeStringId: Int?) {

    CUSTOM(1, R.string.song_category_custom),

    OTHERS(2, R.string.song_category_others),

    ARTIST(3, null);

    companion object {
        fun parseById(id: Long): CategoryType {
            return values().first { v -> v.id == id }
        }
    }
}
