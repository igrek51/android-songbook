package igrek.songbook.admin.antechamber

import igrek.songbook.settings.chordsnotation.ChordsNotation

class AntechamberSong(
        var id: Long,
        var title: String,
        var categoryName: String? = null,
        var content: String? = null,
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
        var chordsNotation: ChordsNotation? = null,
        var originalSongId: Long? = null,
        var status: AntechamberSongStatus
)

enum class AntechamberSongStatus(val id: Long) {

    PROPOSED(1),

    APPROVED(2),

    PUBLISHED(3),

    ABANDONED(4);

    companion object {
        fun parseById(id: Long): AntechamberSongStatus {
            return values().first { v -> v.id == id }
        }
    }
}
