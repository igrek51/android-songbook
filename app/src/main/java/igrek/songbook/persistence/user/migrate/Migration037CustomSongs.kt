package igrek.songbook.persistence.user.migrate

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.persistence.user.custom.CustomSongsDb
import java.io.File
import java.io.FileNotFoundException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Migration037CustomSongs(private val activity: Activity) {

    private val logger = LoggerFactory.logger
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    fun load(): CustomSongsDb? {
        val dbFile = getLocalSongsDbFile()
        if (!dbFile.exists())
            return null

        // open old database
        val oldLocalDb = openLocalSongsDb()

        // get old custom songs
        val tuples = readAllCustomSongs(oldLocalDb)

        oldLocalDb.close()

        val customSongs = tuples.map { tuple ->
            CustomSong(
                    id = tuple[0] as Long,
                    title = tuple[1] as String,
                    categoryName = tuple[15] as String?,
                    content = tuple[3] as String? ?: "",
                    versionNumber = tuple[4] as Long,
                    createTime = tuple[5] as Long,
                    updateTime = tuple[6] as Long,
                    comment = tuple[9] as String?,
                    preferredKey = tuple[10] as String?,
                    author = tuple[13] as String?
            )
        }.toMutableList()

        return CustomSongsDb(customSongs)
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

    private fun openLocalSongsDb(): SQLiteDatabase {
        val dbFile = getLocalSongsDbFile()
        return openDatabase(dbFile)
    }

    private fun openDatabase(dbFile: File): SQLiteDatabase {
        if (!dbFile.exists())
            throw FileNotFoundException("no old db file $dbFile")
        return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
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

                val tuple = mutableListOf(id, title, categoryId, fileContent, versionNumber, createTime, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, stateId, customCategoryName, language, rank, scrollSpeed, initialDelay, metre)
                tuples.add(tuple)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return tuples
    }

    private fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

    private fun getTimestampColumn(cursor: Cursor, name: String): Long {
        // get datetime, convert to long timestamp
        val stringValue = cursor.getString(cursor.getColumnIndexOrThrow(name))
        return try {
            val date = iso8601Format.parse(stringValue)
            date?.time ?: 0
        } catch (e: ParseException) {
            logger.error(e)
            0
        }
    }

    private fun sqlQuery(db: SQLiteDatabase, sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        return db.rawQuery(sql, selectionArgs)
    }
}