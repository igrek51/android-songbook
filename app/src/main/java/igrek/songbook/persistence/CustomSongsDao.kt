package igrek.songbook.persistence

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategory
import igrek.songbook.persistence.songsdb.SongCategoryType
import igrek.songbook.persistence.songsdb.SongStatus
import igrek.songbook.settings.chordsnotation.ChordsNotation
import java.util.*


class CustomSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun getDatabase(): SQLiteDatabase {
        return localDbService.get().openLocalSongsDb()
    }

    private fun mapSongCategory(cursor: Cursor): SongCategory {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val typeId = cursor.getLong(cursor.getColumnIndexOrThrow("type_id"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val custom = getBooleanColumn(cursor, "is_custom")
        val type = SongCategoryType.parseById(typeId)
        return SongCategory(id, type, name, custom)
    }

    fun readAllSongs(categories: List<SongCategory>): MutableList<Song> {
        val songs: MutableList<Song> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_song")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val fileContent = cursor.getString(cursor.getColumnIndexOrThrow("file_content"))
                val versionNumber = cursor.getLong(cursor.getColumnIndexOrThrow("version_number"))
                val createTime = getTimestampColumn(cursor, "create_time")
                val updateTime = getTimestampColumn(cursor, "update_time")
                val custom = getBooleanColumn(cursor, "is_custom")
                val filename = cursor.getString(cursor.getColumnIndexOrThrow("filename"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val preferredKey = cursor.getString(cursor.getColumnIndexOrThrow("preferred_key"))
                val locked = getBooleanColumn(cursor, "is_locked")
                val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lock_password"))
                val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
                val stateId = cursor.getLong(cursor.getColumnIndexOrThrow("state"))
                val categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
                val customCategoryName = cursor.getString(cursor.getColumnIndexOrThrow("custom_category_name"))
                val language = cursor.getString(cursor.getColumnIndexOrThrow("language"))
                val rank = getOptionalDouble(cursor, "rank")
                val scrollSpeed = getOptionalDouble(cursor, "scroll_speed")
                val initialDelay = getOptionalDouble(cursor, "initial_delay")
                val metre = getOptionalString(cursor, "metre")
                val chordsNotationId = getOptionalLong(cursor, "chords_notation")
                val tags = getOptionalString(cursor, "tags")

                val songStatus = SongStatus.parseById(stateId)
                val category = categories.first { category -> category.id == categoryId }
                val chordsNotation = if (chordsNotationId == null) null else ChordsNotation.parseById(chordsNotationId)

                val song = Song(id, title, category, fileContent, versionNumber, createTime, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, songStatus, customCategoryName, language, metre, rank, scrollSpeed, initialDelay, chordsNotation, tags)
                songs.add(song)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songs
    }

    private fun getNextSongId(): Long {
        val mapper: (Cursor) -> Long = { cursor -> cursor.getLong(cursor.getColumnIndexOrThrow("max")) }
        val maxId: Long = queryOneValue(mapper, 0, "SELECT MAX(id) AS max FROM songs_song")
        return maxId + 1
    }

    fun getCategoryByTypeId(categoryTypeId: Long): SongCategory? {
        val mapper: (Cursor) -> SongCategory = { cursor -> mapSongCategory(cursor) }
        return queryOneValue(mapper, null, "SELECT * FROM songs_category WHERE type_id = ?", categoryTypeId)
    }

    fun addCustomSong(song: Song) {
        // auto increment id
        song.id = getNextSongId()
        // insert new song
        val values = ContentValues()
        values.put("id", song.id)
        values.put("title", song.title)
        values.put("category_id", song.category.id)
        values.put("file_content", song.content)
        values.put("version_number", song.versionNumber)
        values.put("create_time", iso8601Format.format(Date(song.createTime)))
        values.put("update_time", iso8601Format.format(Date(song.updateTime)))
        values.put("is_custom", booleanToNum(song.custom))
        values.put("filename", song.filename)
        values.put("comment", song.comment)
        values.put("preferred_key", song.preferredKey)
        values.put("is_locked", booleanToNum(song.locked))
        values.put("lock_password", song.lockPassword)
        values.put("author", song.author)
        values.put("state", song.status.id)
        values.put("custom_category_name", song.customCategoryName)
        values.put("language", song.language)
        values.put("rank", song.rank)
        values.put("scroll_speed", song.scrollSpeed)
        values.put("initial_delay", song.initialDelay)
        values.put("metre", song.metre)
        values.put("tags", song.tags)
        values.put("chords_notation", song.chordsNotation?.id)

        safeInsert("songs_song", values)
    }

    fun updateCustomSong(song: Song) {
        // next version
        song.versionNumber = song.versionNumber + 1
        // insert new song
        val db = getDatabase()
        val values = ContentValues()
        values.put("title", song.title)
        values.put("custom_category_name", song.customCategoryName)
        values.put("file_content", song.content)
        values.put("language", song.language)
        values.put("version_number", song.versionNumber)
        values.put("update_time", iso8601Format.format(Date()))

        val whereArgs: Array<String> = arrayOf(song.id.toString())
        val affectedRows = db.update("songs_song", values, "id = ?", whereArgs)
        if (affectedRows != 1) {
            logger.warn("rows affected by query: $affectedRows")
        }
    }

    fun removeCustomSong(song: Song) {
        val db = getDatabase()
        val whereArgs: Array<String> = arrayOf(song.id.toString())
        val affectedRows = db.delete("songs_song", "id = ?", whereArgs)
        if (affectedRows != 1) {
            logger.warn("rows affected by query: $affectedRows")
        }
    }

}
