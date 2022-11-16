package igrek.songbook.persistence.user.migrate

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.user.favourite.FavouriteSong
import igrek.songbook.persistence.user.favourite.FavouriteSongsDb
import java.io.File

class Migration037Favourites(private val activity: Activity) {

    private val logger = LoggerFactory.logger

    fun load(): FavouriteSongsDb? {
        val dbFile = getLocalSongsDbFile()
        if (!dbFile.exists())
            return null

        // open old database
        val oldLocalDb = openLocalSongsDb() ?: throw RuntimeException("no old custom songs file")

        // get old favourites
        val favouritesTuples = readFavouriteSongs(oldLocalDb)

        oldLocalDb.close()

        val favourites = favouritesTuples.map { favouritesTuple ->
            FavouriteSong(
                songId = favouritesTuple[0] as Long,
                custom = favouritesTuple[1] as Boolean
            )
        }.toMutableList()

        return FavouriteSongsDb(favourites)
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

    private fun openLocalSongsDb(): SQLiteDatabase? {
        val dbFile = getLocalSongsDbFile()
        // if file does not exist - copy initial db from resources
        if (!dbFile.exists())
            return null
        return openDatabase(dbFile)
    }

    private fun openDatabase(songsDbFile: File): SQLiteDatabase? {
        if (!songsDbFile.exists())
            return null
        return SQLiteDatabase.openDatabase(
            songsDbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )
    }

    private fun getBooleanColumn(cursor: Cursor, name: String): Boolean {
        val intValue = cursor.getInt(cursor.getColumnIndexOrThrow(name))
        return intValue != 0
    }

    private fun sqlQuery(
        db: SQLiteDatabase,
        sql: String,
        selectionArgs: Array<String> = arrayOf()
    ): Cursor {
        return db.rawQuery(sql, selectionArgs)
    }

    private fun readFavouriteSongs(db: SQLiteDatabase): MutableList<List<Any?>> {
        val tuples: MutableList<List<Any?>> = mutableListOf()
        try {
            val cursor = sqlQuery(db, "SELECT * FROM favourite_songs")
            while (cursor.moveToNext()) {
                val songId = cursor.getLong(cursor.getColumnIndexOrThrow("song_id"))
                val custom = getBooleanColumn(cursor, "is_custom")
                val tuple = mutableListOf<Any?>(songId, custom)
                tuples.add(tuple)
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return tuples
    }
}