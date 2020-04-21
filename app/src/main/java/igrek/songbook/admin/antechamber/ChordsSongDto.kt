package igrek.songbook.admin.antechamber

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.chordsnotation.ChordsNotation
import kotlinx.serialization.Serializable

@Serializable
data class ChordsSongDto(
        var id: Long? = null,
        var title: String? = null,
        var categories: List<String>? = null,
        var content: String? = null,
        var version_number: Long? = null,
        var create_time: Long? = null,
        var update_time: Long? = null,
        var language: String? = null,
        var chords_notation: Long? = null,
        var author: String? = null,
        var preferred_key: String? = null,
        var metre: String? = null,
        var comment: String? = null,
        var is_locked: Boolean? = null,
        var lock_password: String? = null,
        var scroll_speed: Double? = null,
        var initial_delay: Double? = null,
        var state: Long? = null,
        var rank: Double? = null,
        var tags: String? = null
) {

    companion object {
        fun fromModel(song: Song): ChordsSongDto = ChordsSongDto(
                id = song.id,
                title = song.title,
                categories = emptyList(),
                content = song.content,
                version_number = song.versionNumber,
                create_time = song.createTime,
                update_time = song.updateTime,
                language = song.language,
                chords_notation = (song.chordsNotation ?: ChordsNotation.default).id,
                author = song.author,
                preferred_key = song.preferredKey,
                metre = song.metre,
                comment = song.comment,
                is_locked = song.locked,
                lock_password = song.lockPassword,
                scroll_speed = song.scrollSpeed,
                initial_delay = song.initialDelay,
                state = song.status.id,
                rank = song.rank,
                tags = song.tags
        )
    }
}
