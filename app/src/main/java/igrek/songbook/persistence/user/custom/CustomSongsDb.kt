package igrek.songbook.persistence.user.custom

import igrek.songbook.settings.chordsnotation.ChordsNotation
import kotlinx.serialization.Serializable


@Serializable
data class CustomSongsDb(
    var songs: MutableList<CustomSong> = mutableListOf(),
    var syncSessionData: SyncSessionData = SyncSessionData(),
)

@Serializable
data class CustomSong(
    var id: String,
    var title: String,
    var categoryName: String? = null,
    var content: String = "",
    var versionNumber: Long = 1,
    var createTime: Long = 0, // timestamp in milliseconds
    var updateTime: Long = 0, // timestamp in milliseconds
    var comment: String? = null,
    var preferredKey: String? = null,
    var metre: String? = null,
    var author: String? = null,
    var language: String? = null,
    var scrollSpeed: Double? = null,
    var initialDelay: Double? = null,
    var chordsNotation: ChordsNotation? = null, // mapped to ENGLISH, GERMAN, null, ...
    var originalSongId: String? = null,
) {
    fun displayName(): String {
        return when {
            !categoryName.isNullOrEmpty() -> "$title - $categoryName"
            else -> title
        }
    }

    val artist: String? get() = this.categoryName
    val chordsNotationN: ChordsNotation get() = this.chordsNotation ?: ChordsNotation.default
}

@Serializable
data class SyncSessionData(
    var lastLocalHash: String = "",
    var lastRemoteHash: String = "",
    var localIdToRemoteMap: MutableMap<String, String> = mutableMapOf(),
    var localTrash: MutableMap<String, Long> = mutableMapOf(), // local song ID to timestamp of removal in seconds
)
