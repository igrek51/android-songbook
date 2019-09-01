package igrek.songbook.persistence.general.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.general.mapper.AbstractMapper

abstract class AbstractSqliteDao {

    abstract fun getDatabase(): SQLiteDatabase

    protected val logger: Logger = LoggerFactory.logger

    protected fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    private fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        return getDatabase().rawQuery(sql, selectionArgs)
    }

    protected fun <T> queryOneValue(mapper: (Cursor) -> T, defaultValue: T, sql: String, vararg args: Any): T {
        try {
            val cursor = sqlQuery(sql, *args)
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

    protected fun safeInsert(table: String, values: ContentValues) {
        try {
            val result = getDatabase().insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_NONE)
            if (result == -1L)
                throw SQLException("result -1")
        } catch (e: SQLException) {
            logger.error("SQL insertion error: $e.message")
        }
    }

    protected fun <T> readEntities(query: String, mapper: AbstractMapper<T>): MutableList<T> {
        val entities: MutableList<T> = mutableListOf()
        try {
            val cursor = sqlQuery(query)
            while (cursor.moveToNext()) {
                entities.add(mapper.map(cursor))
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return entities
    }

}
