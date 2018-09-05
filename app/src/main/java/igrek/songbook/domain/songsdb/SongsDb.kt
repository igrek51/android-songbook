package igrek.songbook.domain.songsdb

data class SongsDb(
        val versionNumber: Long = 1,
        val categories: List<SongCategory>,
        val allSongs: List<Song>
)
