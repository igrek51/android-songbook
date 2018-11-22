package igrek.songbook.persistence

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.songsdb.Song
import igrek.songbook.songselection.favourite.FavouriteSongId


class FavouriteSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun getDatabase(): SQLiteDatabase {
        return localDbService.openLocalSongsDb()
    }

    fun readFavouriteSongs(): List<FavouriteSongId> {
        val favouriteSongs: MutableList<FavouriteSongId> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM favourite_songs")
            while (cursor.moveToNext()) {
                val songId = cursor.getLong(cursor.getColumnIndexOrThrow("song_id"))
                val custom = getBooleanColumn(cursor, "is_custom")
                favouriteSongs.add(FavouriteSongId(songId, custom))
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return favouriteSongs
    }

    fun setAsFavourite(song: Song) {
        if (isSongFavourite(song))
            return

        val db = getDatabase()
        val values = ContentValues()
        values.put("song_id", song.id)
        values.put("is_custom", song.custom)
        db.insert("favourite_songs", null, values)
    }

    fun unsetFavourite(song: Song) {
        if (!isSongFavourite(song))
            return

        val db = getDatabase()
        val whereArgs: Array<String> = arrayOf(song.id.toString(), song.custom.toString())
        db.delete("favourite_songs", "song_id = ? AND is_custom = ?", whereArgs)
    }

    fun isSongFavourite(song: Song): Boolean {
        val mapper: (Cursor) -> Boolean = { cursor -> cursor.getColumnIndexOrThrow("count") > 0 }
        return queryOneValue(mapper, false, "SELECT COUNT(*) AS count FROM favourite_songs WHERE song_id = ? AND is_custom = ?", song.id, song.custom)
    }

    fun findFavouriteSong(favouriteSongId: FavouriteSongId, songs: List<Song>): Song? {
        for (song in songs) {
            if (song.id == favouriteSongId.songId && song.custom == favouriteSongId.custom)
                return song
        }
        return null
    }

    fun populateFavouriteSongs(allSongs: List<Song>): List<Song> {
        val favs = mutableListOf<Song>()
        for (favouriteSongId in readFavouriteSongs()) {
            val favouriteSong = findFavouriteSong(favouriteSongId, allSongs)
            if (favouriteSong != null)
                favs.add(favouriteSong)
        }
        return favs
    }

}
