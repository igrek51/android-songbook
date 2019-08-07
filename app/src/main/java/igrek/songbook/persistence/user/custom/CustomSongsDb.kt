package igrek.songbook.persistence.user.custom

import igrek.songbook.settings.chordsnotation.ChordsNotation
import kotlinx.serialization.Serializable


@Serializable
data class CustomSongsDb(
        var songs: MutableList<CustomSong>
)

@Serializable
data class CustomSong(
        var id: Long,
        var title: String,
        var categoryName: String? = null,
        var content: String = "",
        var versionNumber: Long = 1,
        var createTime: Long = 0,
        var updateTime: Long = 0,
        var comment: String? = null,
        var preferredKey: String? = null,
        var metre: String? = null,
        var author: String? = null,
        var language: String? = null,
        var scrollSpeed: Double? = null,
        var initialDelay: Double? = null,
        var chordsNotation: ChordsNotation? = null
)