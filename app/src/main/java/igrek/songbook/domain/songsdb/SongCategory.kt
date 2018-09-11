package igrek.songbook.domain.songsdb

import com.google.common.base.Objects
import igrek.songbook.domain.cache.SimpleCache

open class SongCategory(
        val id: Long,
        val type: SongCategoryType,
        val name: String? = null,
        open var displayName: String? = null,
        var songs: List<Song>? = null
) {

    override fun equals(other: Any?): Boolean {
        if (other !is SongCategory)
            return false
        return Objects.equal(id, other.id)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    private var unlockedSongsCache: SimpleCache<List<Song>> =
            SimpleCache { songs!!.filter { s -> !s.locked } }

    fun getUnlockedSongs(): List<Song> {
        return unlockedSongsCache.get()
    }
}