package igrek.songbook.persistence

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

abstract class AbstractSqliteDao {
    @Inject
    lateinit var localDbService: LocalDbService

    protected val logger: Logger = LoggerFactory.logger
    protected val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    protected abstract fun getDatabase(): SQLiteDatabase

    protected fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    protected fun sqlQueryArray(sql: String, args: Array<out Any>): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    protected fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        val db = getDatabase()
        return db.rawQuery(sql, selectionArgs)
    }

    protected fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

    protected fun getOptionalDouble(cursor: Cursor, columnName: String): Double? {
        val columneIndex = cursor.getColumnIndex(columnName)
        if (columneIndex == -1) {
            return null
        }

        if (cursor.isNull(columneIndex))
            return null

        return cursor.getDouble(columneIndex)
    }

    protected fun getOptionalString(cursor: Cursor, columnName: String): String? {
        val columneIndex = cursor.getColumnIndex(columnName)
        if (columneIndex == -1) {
            return null
        }

        if (cursor.isNull(columneIndex))
            return null

        return cursor.getString(columneIndex)
    }

    protected fun booleanToNum(b: Boolean): Long {
        return if (b) 1 else 0
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

    fun readDbVersionNumber(): Long? {
        val mapper: (Cursor) -> Long = { cursor -> cursor.getLong(cursor.getColumnIndexOrThrow("value")) }
        return queryOneValue(mapper, null, "SELECT value FROM songs_info WHERE name = 'version_number'")
    }

    protected fun <T> queryOneValue(mapper: (Cursor) -> T, defaultValue: T, sql: String, vararg args: Any): T {
        try {
            val cursor = sqlQueryArray(sql, args)
            cursor.use { cursorIn ->
                if (cursorIn.moveToNext()) {
                    return mapper.invoke(cursorIn)
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        } catch (e: SQLiteException) {
            logger.error(e)
        }
        return defaultValue
    }

}
