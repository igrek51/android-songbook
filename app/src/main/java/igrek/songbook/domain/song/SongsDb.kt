package igrek.songbook.domain.song

data class SongsDb(
        val versionNumber: Long = 0,
        val categories: List<SongCategory>? = null,
        val allSongs: List<Song>? = null
)
