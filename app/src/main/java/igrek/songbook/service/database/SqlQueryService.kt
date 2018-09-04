package igrek.songbook.service.database

import android.database.Cursor
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.logger.LoggerFactory
import javax.inject.Inject

class SqlQueryService {

    @Inject
    lateinit var songsDbService: SongsDbService

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun read() {
        try {
            val cursor = sqlQuery("SELECT * FROM songs ORDER BY id")

            while (cursor.moveToNext()) {
                val itemId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                logger.debug(itemId, title)
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
    }

    private fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    private fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        val db = songsDbService.dbHelper.readableDatabase
        return db.rawQuery(sql, selectionArgs)
    }


}
