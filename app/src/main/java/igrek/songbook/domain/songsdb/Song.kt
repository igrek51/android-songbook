package igrek.songbook.domain.songsdb

import com.google.common.base.Objects

open class Song(
        var id: Long,
        open var title: String,
        open var category: SongCategory,
        var fileContent: String? = null,
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
        var status: SongStatus
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Song)
            return false
        return Objects.equal(id, other.id)
                && Objects.equal(category, other.category)
                && Objects.equal(versionNumber, other.versionNumber)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, category.id, versionNumber)
    }

    fun displayName(): String {
        if (category.type == SongCategoryType.ARTIST) {
            return title + " - " + category.name
        } else {
            return title
        }
    }
}