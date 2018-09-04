package igrek.songbook.domain.song

data class SongCategory(
        val name: String? = null,
        val type: SongCategoryType? = null,
        val songs: List<Song>? = null
)