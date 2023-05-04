package igrek.songbook.persistence.general.model

import com.google.common.base.Objects
import igrek.songbook.settings.chordsnotation.ChordsNotation

data class Song(
    var id: String,
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
    var chordsNotation: ChordsNotation = ChordsNotation.default,
    var tags: String? = null,
    var originalSongId: String? = null,
    var namespace: SongNamespace = SongNamespace.Public,
) {

    val artist: String? get() = displayCategories().takeIf { it.isNotEmpty() }

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
        if (categories.isEmpty())
            return false
        return categories.any { c -> c.type == CategoryType.ARTIST }
    }

    fun displayCategories(): String {
        val publicCategories = categories
            .filter { it.type == CategoryType.ARTIST }
            .joinToString(", ") { c -> c.displayName ?: "" }
        if (publicCategories.isNotEmpty())
            return publicCategories
        return customCategoryName.orEmpty()
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

    fun isPublic(): Boolean {
        return namespace == SongNamespace.Public
    }

    fun isAntechamber(): Boolean {
        return namespace == SongNamespace.Antechamber
    }

    override fun toString(): String = displayName()
}