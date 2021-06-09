package igrek.songbook.persistence.general.mapper

import android.database.Cursor
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

abstract class AbstractMapper<T> {

    abstract fun map(cursor: Cursor): T

    protected val logger: Logger = LoggerFactory.logger

    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

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

    protected fun getOptionalLong(cursor: Cursor, columnName: String): Long? {
        val columneIndex = cursor.getColumnIndex(columnName)
        if (columneIndex == -1) {
            return null
        }

        if (cursor.isNull(columneIndex))
            return null

        return cursor.getLong(columneIndex)
    }

    protected fun booleanToNum(b: Boolean): Long {
        return if (b) 1 else 0
    }

    protected fun getTimestampColumn(cursor: Cursor, name: String): Long {
        // get datetime, convert to long timestamp
        val stringValue = cursor.getString(cursor.getColumnIndexOrThrow(name)) ?: return 0
        return try {
            val date = iso8601Format.parse(stringValue)
            date?.time ?: 0
        } catch (e: ParseException) {
            logger.error(e)
            0
        }
    }

}