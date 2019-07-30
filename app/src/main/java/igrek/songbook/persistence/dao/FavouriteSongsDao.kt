package igrek.songbook.persistence.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.songselection.favourite.FavouriteSongId


class FavouriteSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun getDatabase(): SQLiteDatabase {
        return localDbService.get().openLocalSongsDb()
    }

    private fun readFavouriteSongs(): List<FavouriteSongId> {
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

        val values = ContentValues()
        values.put("song_id", song.id)
        values.put("is_custom", booleanToNum(song.custom))

        safeInsert("favourite_songs", values)
    }

    fun unsetFavourite(song: Song) {
        if (!isSongFavourite(song))
            return

        val db = getDatabase()
        val isCustom = booleanToNum(song.custom).toString()
        val whereArgs: Array<String> = arrayOf(song.id.toString(), isCustom)
        val affectedRows = db.delete("favourite_songs", "song_id = ? AND is_custom = ?", whereArgs)
        if (affectedRows != 1) {
            logger.warn("rows affected by query: $affectedRows")
        }
    }

    private fun isSongFavourite(song: Song): Boolean {
        val mapper: (Cursor) -> Boolean = { cursor ->
            val count = cursor.getLong(cursor.getColumnIndexOrThrow("count"))
            count > 0
        }
        val isCustom = booleanToNum(song.custom)
        return queryOneValue(mapper, false, "SELECT COUNT(*) AS count FROM favourite_songs WHERE song_id = ? AND is_custom = ?", song.id, isCustom)
    }

    private fun findFavouriteSong(favouriteSongId: FavouriteSongId, songs: List<Song>): Song? {
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
