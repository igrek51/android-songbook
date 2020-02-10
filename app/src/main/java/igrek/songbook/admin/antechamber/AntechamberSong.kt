package igrek.songbook.admin.antechamber

import igrek.songbook.settings.chordsnotation.ChordsNotation

data class AntechamberSong(
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

data class AntechamberSongDto(
        var id: Long,
        var title: String,
        var category_name: String? = null,
        var content: String? = null,
        var version_number: Long = 1,
        var create_time: Long = 0,
        var update_time: Long = 0,
        var comment: String? = null,
        var preferred_key: String? = null,
        var metre: String? = null,
        var author: String? = null,
        var language: String? = null,
        var scroll_speed: Double? = null,
        var initial_delay: Double? = null,
        var chords_notation: Long? = null,
        var original_song_id: Long? = null,
        var status: Long
) {
    fun toModel(): AntechamberSong {
        return AntechamberSong(
                id = id,
                title = title,
                categoryName = category_name,
                content = content,
                versionNumber = version_number,
                createTime = create_time,
                updateTime = update_time,
                comment = comment,
                preferredKey = preferred_key,
                metre = metre,
                author = author,
                language = language,
                scrollSpeed = scroll_speed,
                initialDelay = initial_delay,
                chordsNotation = ChordsNotation.parseById(chords_notation),
                originalSongId = original_song_id,
                status = AntechamberSongStatus.parseById(status)
        )
    }
}

data class AllAntechamberSongsDto(
        var songs: List<AntechamberSongDto> = emptyList()
) {
    fun toModel(): List<AntechamberSong> {
        return songs.map { dto -> dto.toModel() }.toMutableList()
    }
}
