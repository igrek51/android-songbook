package igrek.songbook.persistence.model

import com.google.common.base.Objects

open class SongId(
        var id: Long,
        var custom: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other !is SongId)
            return false
        return Objects.equal(id, other.id)
                && Objects.equal(custom, other.custom)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, custom)
    }
}