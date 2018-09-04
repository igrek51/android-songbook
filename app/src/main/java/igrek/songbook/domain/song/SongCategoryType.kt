package igrek.songbook.domain.song

enum class SongCategoryType(val id: Long, val localeStringId: String?) {

    ARTIST(1, null),

    CUSTOM(2, "song_category_custom"),

    OTHERS(3, "song_category_others")
}
