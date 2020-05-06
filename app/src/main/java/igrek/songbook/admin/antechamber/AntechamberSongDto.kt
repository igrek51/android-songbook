package igrek.songbook.admin.antechamber

import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import kotlinx.serialization.Serializable

@Serializable
data class AntechamberSongDto(
        var id: Long? = null,
        var title: String? = null,
        var category_name: String? = null,
        var content: String? = null,
        var version_number: Long? = 1,
        var create_time: Long? = 0,
        var update_time: Long? = 0,
        var comment: String? = null,
        var preferred_key: String? = null,
        var metre: String? = null,
        var author: String? = null,
        var language: String? = null,
        var scroll_speed: Double? = null,
        var initial_delay: Double? = null,
        var chords_notation: Long? = null,
        var original_song_id: Long? = null,
        var status: Long? = null,
        var categories: List<Long>? = null,
) {
    fun toModel(): Song = Song(
            id = id!!,
            title = title!!,
            categories = mutableListOf(),
            content = content,
            versionNumber = version_number!!,
            createTime = create_time!!,
            updateTime = update_time!!,
            comment = comment,
            preferredKey = preferred_key,
            author = author,
            status = SongStatus.PROPOSED,
            customCategoryName = category_name,
            language = language,
            metre = metre,
            scrollSpeed = scroll_speed,
            initialDelay = initial_delay,
            chordsNotation = ChordsNotation.parseById(chords_notation),
            originalSongId = original_song_id,
            namespace = SongNamespace.Antechamber,
    )

    companion object {
        fun fromModel(song: Song): AntechamberSongDto = AntechamberSongDto(
                id = song.id,
                title = song.title,
                category_name = song.customCategoryName,
                content = song.content,
                version_number = song.versionNumber,
                create_time = song.createTime,
                update_time = song.updateTime,
                author = song.author,
                language = song.language,
                chords_notation = (song.chordsNotation ?: ChordsNotation.default).id,
                original_song_id = song.originalSongId,
                status = song.status.id,
                categories = song.categories.filter { it.type == CategoryType.ARTIST }.map { it.id },
        )
    }
}

@Serializable
data class AllAntechamberSongsDto(
        var songs: List<AntechamberSongDto> = emptyList()
) {
    fun toModel(): List<Song> {
        return songs.map { dto -> dto.toModel() }.toMutableList()
    }
}
