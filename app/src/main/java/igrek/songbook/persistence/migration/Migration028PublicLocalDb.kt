package igrek.songbook.persistence.migration

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.info.logger.LoggerFactory
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Migration028PublicLocalDb(val activity: Activity) : IMigration {

    private var customSongsDb: SQLiteDatabase? = null
    private val logger = LoggerFactory.getLogger()
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun migrate(migrator: DatabaseMigrator) {
        // get custom songs only
        customSongsDb = openCustomSongsDb() ?: return

        val tuples = readAllCustomSongs()
        customSongsDb!!.close()

        migrator.makeFactoryReset()

        // insert old tuples
        for (tuple in tuples) {
            saveCustomSong(tuple)
        }

        removeDb(getCustomSongsDbFile())
    }

    private fun getCustomSongsDbFile(): File {
        return File(getSongDbDir(), "custom_songs.sqlite")
    }

    @SuppressLint("SdCardPath")
    private fun getSongDbDir(): File {
        val dir: File?

        // /data/data/PACKAGE/files
        dir = activity.getFilesDir()
        if (dir != null && dir.isDirectory)
            return dir

        return File("/data/data/" + activity.getPackageName() + "/files")
    }

    fun openCustomSongsDb(): SQLiteDatabase? {
        val dbFile = getCustomSongsDbFile()
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

    fun readAllCustomSongs(): MutableList<List<Any>> {
        val tuples: MutableList<List<Any>> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_song ORDER BY id")

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

                val tuple = listOf<Any>(id, title, categoryId, fileContent, versionNumber, createTime, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, stateId)
                tuples.add(tuple)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return tuples
    }

    fun saveCustomSong(tuple: List<Any>) {
        val values = ContentValues()
        values.put("id", tuple[0] as Long)
        values.put("title", tuple[1] as String)
        values.put("category_id", tuple[2] as Long)
        values.put("file_content", tuple[3] as String)
        values.put("version_number", tuple[4] as Long)
        values.put("create_time", iso8601Format.format(Date(tuple[5] as Long)))
        values.put("update_time", iso8601Format.format(Date(tuple[6] as Long)))
        values.put("is_custom", tuple[7] as Boolean)
        values.put("filename", tuple[8] as String)
        values.put("comment", tuple[9] as String)
        values.put("preferred_key", tuple[10] as String)
        values.put("is_locked", tuple[11] as Boolean)
        values.put("lock_password", tuple[12] as String)
        values.put("author", tuple[13] as String)
        values.put("state", tuple[14] as Long)

        customSongsDb!!.insert("songs_song", null, values)
    }

    protected fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        return customSongsDb!!.rawQuery(sql, selectionArgs)
    }


    protected fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

    protected fun getTimestampColumn(cursor: Cursor, name: String): Long {
        // get datetime, convert to long timestamp
        val stringValue = cursor.getString(cursor.getColumnIndexOrThrow(name))
        try {
            val date = iso8601Format.parse(stringValue)
            return date.time
        } catch (e: ParseException) {
            logger.error(e)
            return 0
        }
    }

    private fun removeDb(songsDbFile: File) {
        if (songsDbFile.exists()) {
            if (!songsDbFile.delete() || songsDbFile.exists())
                logger.error("failed to delete database file: " + songsDbFile.absolutePath)
        }
    }
}