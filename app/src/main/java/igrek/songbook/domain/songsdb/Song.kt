package igrek.songbook.domain.songsdb

import com.google.common.base.Objects

data class Song(
        val id: Long,
        var title: String,
        var category: SongCategory,
        var fileContent: String? = null,
        var versionNumber: Long = 1,
        var updateTime: Long = 0,
        var custom: Boolean = false,
        var filename: String? = null,
        var comment: String? = null,
        var preferredKey: String? = null,
        var locked: Boolean = false,
        var lockPassword: String? = null,
        var author: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Song)
            return false
        return Objects.equal(id, other.id) && Objects.equal(versionNumber, other.versionNumber)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, versionNumber)
    }
}