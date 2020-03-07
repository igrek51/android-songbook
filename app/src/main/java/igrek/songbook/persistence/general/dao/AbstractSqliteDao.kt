package igrek.songbook.persistence.general.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.general.mapper.AbstractMapper

abstract class AbstractSqliteDao {

    abstract fun getDatabase(): SQLiteDatabase

    protected val logger: Logger = LoggerFactory.logger

    private fun sqlQuery(sql: String, vararg args: Any): Cursor {
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
            throw e
        } catch (e: SQLiteException) {
            logger.error(e)
            throw e
        }
        return defaultValue
    }

    protected fun count(table: String): Long? {
        val mapper: (Cursor) -> Long = { cursor -> cursor.getLong(0) }
        return queryOneValue(mapper, null, "SELECT count(*) FROM $table")
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
            throw e
        }
        return entities
    }

}
