package igrek.songbook.persistence.model

import com.google.common.base.Objects
import igrek.songbook.system.cache.SimpleCache

open class Category(
        val id: Long,
        open val type: CategoryType,
        open val name: String? = null,
        var custom: Boolean = false,
        open var displayName: String? = null,
        var songs: MutableList<Song> = mutableListOf()
) {

    override fun equals(other: Any?): Boolean {
        if (other !is Category)
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