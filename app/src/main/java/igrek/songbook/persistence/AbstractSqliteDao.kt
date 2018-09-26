package igrek.songbook.persistence

import android.database.Cursor
import igrek.songbook.info.logger.LoggerFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import javax.inject.Inject

abstract class AbstractSqliteDao {
    @Inject
    lateinit var localDbService: LocalDbService

    protected val logger = LoggerFactory.getLogger()
    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    protected abstract fun getDbHelper(): SQLiteDbHelper

    protected fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    protected fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        val db = getDbHelper().readableDatabase
        return db.rawQuery(sql, selectionArgs)
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

    fun readDbVersionNumber(): Long? {
        try {
            val cursor = sqlQuery("SELECT value FROM songs_info WHERE name = 'version_number'")
            try {
                if (cursor.moveToNext()) {
                    return cursor.getLong(cursor.getColumnIndexOrThrow("value"))
                }
            } finally {
                cursor.close()
            }
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return null
    }
}
