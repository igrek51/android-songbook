package igrek.songbook.persistence.general.model

import com.google.common.base.Objects
import igrek.songbook.settings.chordsnotation.ChordsNotation

data class Song(
        var id: Long,
        var title: String,
        var categories: MutableList<Category> = mutableListOf(),
        var content: String? = null,
        var versionNumber: Long = 1,
        var createTime: Long = 0, // timestamp in milliseconds
        var updateTime: Long = 0, // timestamp in milliseconds
        var comment: String? = null,
        var preferredKey: String? = null,
        var locked: Boolean = false,
        var lockPassword: String? = null,
        var author: String? = null,
        var status: SongStatus,
        var customCategoryName: String? = null,
        var language: String? = null,
        var metre: String? = null,
        var rank: Double? = null,
        var scrollSpeed: Double? = null,
        var initialDelay: Double? = null,
        var chordsNotation: ChordsNotation? = null,
        var tags: String? = null,
        var originalSongId: Long? = null,
        var namespace: SongNamespace = SongNamespace.Public
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Song)
            return false
        return Objects.equal(id, other.id)
                && Objects.equal(namespace, other.namespace)
                && Objects.equal(versionNumber, other.versionNumber)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, namespace, versionNumber)
    }

    private fun hasArtistCategory(): Boolean {
        if (categories.isNullOrEmpty())
            return false
        return categories.any { c -> c.type == CategoryType.ARTIST }
    }

    fun displayCategories(): String {
        return categories.joinToString(", ") { c -> c.displayName ?: "" }
    }

    fun displayName(): String {
        return when {
            hasArtistCategory() -> "$title - ${displayCategories()}"
            !customCategoryName.isNullOrEmpty() -> "$title - $customCategoryName"
            else -> title
        }
    }

    fun songIdentifier(): SongIdentifier {
        return SongIdentifier(id, namespace)
    }

    fun isCustom(): Boolean {
        return namespace == SongNamespace.Custom
    }
}