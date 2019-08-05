package igrek.songbook.persistence.model

import com.google.common.base.Objects
import igrek.songbook.settings.chordsnotation.ChordsNotation

open class Song(
        var id: Long,
        open var title: String,
        open var categories: MutableList<Category> = mutableListOf(),
        var content: String? = null,
        var versionNumber: Long = 1,
        var createTime: Long = 0,
        var updateTime: Long = 0,
        var custom: Boolean = false,
        var comment: String? = null,
        var preferredKey: String? = null,
        var locked: Boolean = false,
        var lockPassword: String? = null,
        var author: String? = null,
        var state: SongStatus,
        var customCategoryName: String? = null,
        var language: String? = null,
        var metre: String? = null,
        var rank: Double? = null,
        var scrollSpeed: Double? = null,
        var initialDelay: Double? = null,
        var chordsNotation: ChordsNotation? = null,
        var tags: String? = null
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

    private fun hasArtistCategory(): Boolean {
        if (categories.isNullOrEmpty())
            return false
        return categories.any { c -> c.type == CategoryType.ARTIST }
    }

    private fun displayCategories(): String {
        return categories.joinToString(", ") { c -> c.displayName!! }
    }

    fun displayName(): String {
        return when {
            hasArtistCategory() -> "$title - ${displayCategories()}"
            custom and !customCategoryName.isNullOrEmpty() -> "$title - $customCategoryName"
            else -> title
        }
    }
}