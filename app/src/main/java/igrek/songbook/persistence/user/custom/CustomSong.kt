package igrek.songbook.persistence.user.custom

import igrek.songbook.settings.chordsnotation.ChordsNotation
import kotlinx.serialization.*

@Serializable
data class CustomSong(
        val id: Long,
        val title: String,
        val categoryName: String? = null,
        val content: String = "",
        val versionNumber: Long = 1,
        val createTime: Long = 0,
        val updateTime: Long = 0,
        val comment: String? = null,
        val preferredKey: String? = null,
        var metre: String? = null,
        val author: String? = null,
        val language: String? = null,
        var scrollSpeed: Double? = null,
        var initialDelay: Double? = null,
        var chordsNotation: ChordsNotation? = null
)