package igrek.songbook.model.songsdb

import com.google.common.base.Objects

open class FavouriteSongId(
        var songId: Long,
        var custom: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other !is FavouriteSongId)
            return false
        return Objects.equal(songId, other.songId)
                && Objects.equal(custom, other.custom)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(songId, custom)
    }

}