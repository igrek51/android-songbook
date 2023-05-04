package igrek.songbook.persistence.general.mapper

import android.database.Cursor
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation

class SongMapper : AbstractMapper<Song>() {

    override fun map(cursor: Cursor): Song {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
        val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
        val versionNumber = cursor.getLong(cursor.getColumnIndexOrThrow("version_number"))
        val createTime = getTimestampColumn(cursor, "create_time")
        val updateTime = getTimestampColumn(cursor, "update_time")
        val language = cursor.getString(cursor.getColumnIndexOrThrow("language"))
        val chordsNotationId = getOptionalLong(cursor, "chords_notation")
        val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
        val preferredKey = cursor.getString(cursor.getColumnIndexOrThrow("preferred_key"))
        val metre = getOptionalString(cursor, "metre")
        val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
        val locked = getBooleanColumn(cursor, "is_locked")
        val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lock_password"))
        val scrollSpeed = getOptionalDouble(cursor, "scroll_speed")
        val initialDelay = getOptionalDouble(cursor, "initial_delay")
        val stateId = cursor.getLong(cursor.getColumnIndexOrThrow("state"))
        val rank = getOptionalDouble(cursor, "rank")
        val tags = getOptionalString(cursor, "tags")

        val songStatus = SongStatus.parseById(stateId) ?: SongStatus.PUBLISHED
        val chordsNotation = ChordsNotation.mustParseById(chordsNotationId)

        return Song(
            id = id.toString(),
            title = title,
            categories = mutableListOf(),
            content = content,
            versionNumber = versionNumber,
            createTime = createTime,
            updateTime = updateTime,
            comment = comment,
            preferredKey = preferredKey,
            locked = locked,
            lockPassword = lockPassword,
            author = author,
            status = songStatus,
            customCategoryName = null,
            language = language,
            metre = metre,
            rank = rank,
            scrollSpeed = scrollSpeed,
            initialDelay = initialDelay,
            chordsNotation = chordsNotation,
            tags = tags,
            namespace = SongNamespace.Public
        )
    }
}