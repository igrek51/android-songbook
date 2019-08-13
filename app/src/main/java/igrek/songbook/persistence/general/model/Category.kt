package igrek.songbook.persistence.general.model

import com.google.common.base.Objects
import igrek.songbook.util.lookup.SimpleCache

open class Category(
        val id: Long = 0,
        val type: CategoryType,
        val name: String? = null,
        var custom: Boolean = false,
        var songs: MutableList<Song> = mutableListOf()
) {
    var displayName: String? = null
        get() {
            if (field == null)
                return name
            return field
        }

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
            SimpleCache { songs.filter { s -> !s.locked } }

    fun getUnlockedSongs(): List<Song> {
        return unlockedSongsCache.get()
    }
}