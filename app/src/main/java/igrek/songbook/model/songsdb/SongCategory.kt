package igrek.songbook.model.songsdb

import com.google.common.base.Objects
import igrek.songbook.system.cache.SimpleCache

open class SongCategory(
        val id: Long,
        open val type: SongCategoryType,
        open val name: String? = null,
        var custom: Boolean = false,
        open var displayName: String? = null,
        var songs: List<Song>? = null
) {

    override fun equals(other: Any?): Boolean {
        if (other !is SongCategory)
            return false
        return Objects.equal(id, other.id)
                && Objects.equal(custom, other.custom)
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