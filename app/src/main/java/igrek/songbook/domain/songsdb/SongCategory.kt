package igrek.songbook.domain.songsdb

import com.google.common.base.Objects

data class SongCategory(
        val id: Long,
        val type: SongCategoryType,
        val name: String? = null,
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
}