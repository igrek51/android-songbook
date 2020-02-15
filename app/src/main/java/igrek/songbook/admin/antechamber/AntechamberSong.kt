package igrek.songbook.admin.antechamber

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation

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
    fun toModel(): Song = Song(
            id = id,
            title = title,
            categories = mutableListOf(),
            content = content,
            versionNumber = version_number,
            createTime = create_time,
            updateTime = update_time,
            comment = comment,
            preferredKey = preferred_key,
            author = author,
            state = SongStatus.PROPOSED,
            customCategoryName = category_name,
            language = language,
            metre = metre,
            scrollSpeed = scroll_speed,
            initialDelay = initial_delay,
            chordsNotation = ChordsNotation.parseById(chords_notation),
            originalSongId = original_song_id,
            namespace = SongNamespace.Antechamber
    )
}

data class AllAntechamberSongsDto(
        var songs: List<AntechamberSongDto> = emptyList()
) {
    fun toModel(): List<Song> {
        return songs.map { dto -> dto.toModel() }.toMutableList()
    }
}
