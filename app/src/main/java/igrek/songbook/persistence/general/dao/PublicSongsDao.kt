package igrek.songbook.persistence.general.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openDatabase
import igrek.songbook.persistence.general.mapper.CategoryMapper
import igrek.songbook.persistence.general.mapper.SongCategoryMapper
import igrek.songbook.persistence.general.mapper.SongMapper
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongCategoryRelationship
import java.io.File


class PublicSongsDao(private val dbFile: File) : AbstractSqliteDao() {

    private val songMapper = SongMapper()
    private val categoryMapper = CategoryMapper()
    private val songCategoryMapper = SongCategoryMapper()

    private var songsDbHelper: SQLiteDatabase? = null

    private val supportedDbVersion = 50

    override fun getDatabase(): SQLiteDatabase {
        if (songsDbHelper == null)
            songsDbHelper = openDatabase(dbFile)
        return songsDbHelper!!
    }

    private fun openDatabase(songsDbFile: File): SQLiteDatabase {
        if (!songsDbFile.exists())
            throw NoSuchFileException(songsDbFile, null, "Database file does not exist: ${songsDbFile.absolutePath}")
        val db = openDatabase(songsDbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        db.disableWriteAheadLogging()
        return db
    }

    fun close() {
        songsDbHelper?.close()
        songsDbHelper = null
    }

    fun readAllCategories(): MutableList<Category> {
        return readEntities("SELECT * FROM songs_category", categoryMapper)
    }

    fun readAllSongs(): MutableList<Song> {
        return readEntities("SELECT * FROM songs_song", songMapper)
    }

    fun readAllSongCategories(): MutableList<SongCategoryRelationship> {
        return readEntities("SELECT * FROM songs_song_categories", songCategoryMapper)
    }

    fun readDbVersionNumber(): Long? {
        val mapper: (Cursor) -> Long = { cursor -> cursor.getLong(cursor.getColumnIndexOrThrow("value")) }
        return queryOneValue(mapper, null, "SELECT value FROM songs_info WHERE name = 'version_number'")
    }

    fun verifyDbVersion(dbVersion: Long) {
        if (dbVersion < supportedDbVersion)
            throw RuntimeException("local db version $dbVersion is not supported anymore")
    }

}
