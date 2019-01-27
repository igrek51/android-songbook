package igrek.songbook.persistence.migration

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.info.logger.LoggerFactory
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Migration037PublicLocalDb(val activity: Activity) : IMigration {

    private val logger = LoggerFactory.logger
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    override fun migrate(migrator: DatabaseMigrator) {
        // open old database
        val oldLocalDb = openLocalSongsDb() ?: throw RuntimeException("no old custom songs file")

        // get old custom songs
        val tuples = readAllCustomSongs(oldLocalDb)
        // get old favourites
        val favouritesTuples = readFavouriteSongs(oldLocalDb)

        oldLocalDb.close()

        // clear
        migrator.makeFactoryReset()

        // insert old tuples to new local db
        val newLocalDb = migrator.songsRepository!!.localDbService.get().openLocalSongsDb()

        for (tuple in tuples) {
            addNewCustomSong(newLocalDb, tuple)
        }

        for (favouritesTuple in favouritesTuples) {
            setAsFavourite(newLocalDb, favouritesTuple)
        }

        // reinitialize db
        migrator.songsRepository!!.initializeSongsDb()
    }

    private fun getLocalSongsDbFile(): File {
        return File(getSongDbDir(), "local.sqlite")
    }

    @SuppressLint("SdCardPath")
    private fun getSongDbDir(): File {
        // /data/data/PACKAGE/files
        val dir: File? = activity.filesDir
        if (dir != null && dir.isDirectory)
            return dir

        return File("/data/data/" + activity.packageName + "/files")
    }

    fun openLocalSongsDb(): SQLiteDatabase? {
        val dbFile = getLocalSongsDbFile()
        // if file does not exist - copy initial db from resources
        if (!dbFile.exists())
            return null
        return openDatabase(dbFile)
    }

    private fun openDatabase(songsDbFile: File): SQLiteDatabase? {
        if (!songsDbFile.exists())
            return null
        return SQLiteDatabase.openDatabase(songsDbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun readAllCustomSongs(db: SQLiteDatabase): MutableList<List<Any?>> {
        val tuples: MutableList<List<Any?>> = mutableListOf()
        try {
            val cursor = sqlQuery(db, "SELECT * FROM songs_song ORDER BY id")

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
                val language = null
                val rank = null
                val scrollSpeed = null
                val initialDelay = null
                val metre = null

                val tuple = mutableListOf<Any?>(id, title, categoryId, fileContent, versionNumber, createTime, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, stateId, customCategoryName, language, rank, scrollSpeed, initialDelay, metre)
                tuples.add(tuple)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return tuples
    }

    private fun addNewCustomSong(db: SQLiteDatabase, tuple: List<Any?>) {
        // insert new song
        val values = ContentValues()
        values.put("id", tuple[0] as Long)
        values.put("title", tuple[1] as String)
        values.put("category_id", tuple[2] as Long)
        values.put("file_content", tuple[3] as String?)
        values.put("version_number", tuple[4] as Long)
        values.put("create_time", iso8601Format.format(Date(tuple[5] as Long)))
        values.put("update_time", iso8601Format.format(Date(tuple[6] as Long)))
        values.put("is_custom", tuple[7] as Boolean)
        values.put("filename", tuple[8] as String?)
        values.put("comment", tuple[9] as String?)
        values.put("preferred_key", tuple[10] as String?)
        values.put("is_locked", tuple[11] as Boolean)
        values.put("lock_password", tuple[12] as String?)
        values.put("author", tuple[13] as String?)
        values.put("state", tuple[14] as Long)
        values.put("custom_category_name", tuple[15] as String?)
        values.put("language", null as String?)
        values.put("rank", null as Double?)
        values.put("scroll_speed", null as Double?)
        values.put("initial_delay", null as Double?)
        values.put("metre", null as String?)

        safeInsert(db, "songs_song", values)
    }

    protected fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

    protected fun getTimestampColumn(cursor: Cursor, name: String): Long {
        // get datetime, convert to long timestamp
        val stringValue = cursor.getString(cursor.getColumnIndexOrThrow(name))
        return try {
            val date = iso8601Format.parse(stringValue)
            date.time
        } catch (e: ParseException) {
            logger.error(e)
            0
        }
    }

    private fun removeDb(songsDbFile: File) {
        if (songsDbFile.exists()) {
            if (!songsDbFile.delete() || songsDbFile.exists())
                logger.error("failed to delete database file: " + songsDbFile.absolutePath)
        }
    }

    protected fun safeInsert(db: SQLiteDatabase, table: String, values: ContentValues) {
        try {
            val result = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_NONE)
            if (result == -1L)
                throw SQLException("result -1")
        } catch (e: SQLException) {
            logger.error("SQL insertion error: $e.message")
        }
    }

    protected fun sqlQuery(db: SQLiteDatabase, sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        return db.rawQuery(sql, selectionArgs)
    }

    private fun readFavouriteSongs(db: SQLiteDatabase): MutableList<List<Any?>> {
        val tuples: MutableList<List<Any?>> = mutableListOf()
        try {
            val cursor = sqlQuery(db, "SELECT * FROM favourite_songs")
            while (cursor.moveToNext()) {
                val songId = cursor.getLong(cursor.getColumnIndexOrThrow("song_id"))
                val custom = getBooleanColumn(cursor, "is_custom")
                val tuple = mutableListOf<Any?>(songId, custom)
                tuples.add(tuple)
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return tuples
    }

    fun setAsFavourite(db: SQLiteDatabase, tuple: List<Any?>) {
        val values = ContentValues()
        values.put("song_id", tuple[0] as Long)
        values.put("is_custom", tuple[1] as Boolean)

        safeInsert(db, "favourite_songs", values)
    }
}