package igrek.songbook.persistence

import android.content.ContentValues
import android.database.Cursor
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongCategoryType
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject


class SqlQueryService {

    @Inject
    lateinit var localDatabaseService: dagger.Lazy<LocalDatabaseService>

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    /*
    SCHEMA:
    c.execute('''CREATE TABLE songs (
		id integer PRIMARY KEY,
		title text NOT NULL,
		categoryId integer NOT NULL,
		fileContent text,
		versionNumber integer NOT NULL,
		updateTime integer,
		custom integer NOT NULL,
		filename text,
		comment text,
		preferredKey text,
		locked integer NOT NULL,
		lockPassword text,
		author text
		);''')
	c.execute('''CREATE TABLE categories (
		id integer PRIMARY KEY,
		typeId integer NOT NULL,
		name text
		);''')
	c.execute('''CREATE TABLE info (
		name text,
		value text
		);''')
     */

    fun readAllCategories(): List<SongCategory> {
        val entities: MutableList<SongCategory> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM categories ORDER BY id")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val typeId = cursor.getLong(cursor.getColumnIndexOrThrow("typeId"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val type = SongCategoryType.parseById(typeId)

                val entity = SongCategory(id, type, name)
                entities.add(entity)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return entities
    }

    fun readAllSongs(categories: List<SongCategory>): List<Song> {
        val songs: MutableList<Song> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs ORDER BY id")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("categoryId"))
                val fileContent = cursor.getString(cursor.getColumnIndexOrThrow("fileContent"))
                val versionNumber = cursor.getLong(cursor.getColumnIndexOrThrow("versionNumber"))
                val updateTime = cursor.getLong(cursor.getColumnIndexOrThrow("updateTime"))
                val custom = getBooleanColumn(cursor, "custom")
                val filename = cursor.getString(cursor.getColumnIndexOrThrow("filename"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val preferredKey = cursor.getString(cursor.getColumnIndexOrThrow("preferredKey"))
                val locked = getBooleanColumn(cursor, "locked")
                val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lockPassword"))
                val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))

                val category = categories.first { category -> category.id == categoryId }

                val song = Song(id, title, category, fileContent, versionNumber, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author)
                songs.add(song)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songs
    }

    fun readDbVersionNumber(): Long? {
        try {
            val cursor = sqlQuery("SELECT value FROM info WHERE name = 'versionNumber'")
            if (cursor.moveToNext()) {
                val value = cursor.getLong(cursor.getColumnIndexOrThrow("value"))
                return value
            }
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return null
    }

    fun unlockSong(id: Long) {
        val db = localDatabaseService.get().dbHelper.writableDatabase
        val values = ContentValues()
        values.put("locked", 0)
        db.update("songs",
                values,
                "id = ?",
                arrayOf(id.toString()))
    }

    private fun sqlQuery(sql: String, vararg args: Any): Cursor {
        val strings: Array<String> = args.map { arg -> arg.toString() }.toTypedArray()
        return sqlQuery(sql, strings)
    }

    private fun sqlQuery(sql: String, selectionArgs: Array<String> = arrayOf()): Cursor {
        val db = localDatabaseService.get().dbHelper.readableDatabase
        return db.rawQuery(sql, selectionArgs)
    }

    private fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

}
