package igrek.songbook.model.songsdb

import com.google.common.base.Objects

open class Song(
        var id: Long,
        open var title: String,
        open var category: SongCategory,
        var content: String? = null,
        var versionNumber: Long = 1,
        var createTime: Long = 0,
        var updateTime: Long = 0,
        var custom: Boolean = false,
        var filename: String? = null,
        var comment: String? = null,
        var preferredKey: String? = null,
        var locked: Boolean = false,
        var lockPassword: String? = null,
        var author: String? = null,
        var status: SongStatus,
        var customCategoryName: String? = null,
        var language: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Song)
            return false
        return Objects.equal(id, other.id)
                && Objects.equal(custom, other.custom)
                && Objects.equal(versionNumber, other.versionNumber)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, custom, versionNumber)
    }

    fun displayName(): String {
        return if (category.type == SongCategoryType.ARTIST) {
            title + " - " + category.name
        } else {
            title
        }
    }
}