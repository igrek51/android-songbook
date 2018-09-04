package igrek.songbook.service.database

import android.database.Cursor
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.song.Song
import igrek.songbook.logger.LoggerFactory
import javax.inject.Inject

class SqlQueryService {

    @Inject
    lateinit var songsDbService: SongsDbService

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    /*
    SCHEMA:
    CREATE TABLE songs (
		id integer PRIMARY KEY,
		fileContent text NOT NULL,
		title text NOT NULL,
		categoryName text,
		versionNumber integer NOT NULL,
		updateTime integer,
		custom integer NOT NULL,
		filename text,
		comment text,
		preferredKey text,
		locked integer NOT NULL,
		lockPassword text
		);
	CREATE TABLE info (
		name text,
		value text
		);
     */

    fun readAllSongs(): List<Song> {
        val songs: MutableList<Song> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs ORDER BY id")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val fileContent = cursor.getString(cursor.getColumnIndexOrThrow("fileContent"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("categoryName"))
                val versionNumber = cursor.getLong(cursor.getColumnIndexOrThrow("versionNumber"))
                val updateTime = cursor.getLong(cursor.getColumnIndexOrThrow("updateTime"))
                val custom = getBooleanColumn(cursor, "custom")
                val filename = cursor.getString(cursor.getColumnIndexOrThrow("filename"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val preferredKey = cursor.getString(cursor.getColumnIndexOrThrow("preferredKey"))
                val locked = getBooleanColumn(cursor, "locked")
                val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lockPassword"))

                val song = Song(id, fileContent, title, categoryName, versionNumber, updateTime, custom, filename, comment, preferredKey, locked, lockPassword)
                songs.add(song)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songs
    }

    private fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    private fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        val db = songsDbService.dbHelper.readableDatabase
        return db.rawQuery(sql, selectionArgs)
    }

    private fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

}
