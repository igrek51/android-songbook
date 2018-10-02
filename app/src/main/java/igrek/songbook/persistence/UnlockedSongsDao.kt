package igrek.songbook.persistence

import android.content.ContentValues
import android.database.Cursor
import igrek.songbook.dagger.DaggerIoc


class UnlockedSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun getDbHelper(): SQLiteDbHelper {
        return localDbService.openUnlockedSongsDb()
    }

    fun readUnlockedSongIds(): List<Long> {
        val songIds: MutableList<Long> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_unlocked")
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                songIds.add(id)
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songIds
    }

    fun unlockSong(id: Long) {
        if (isSongUnlocked(id))
            return
        // if song is not locked
        val db = getDbHelper().writableDatabase
        val values = ContentValues()
        values.put("id", id)
        db.insert("songs_unlocked", null, values)
    }

    fun isSongUnlocked(id: Long): Boolean {
        val mapper: (Cursor) -> Boolean = { cursor -> cursor.getColumnIndexOrThrow("count") > 0 }
        return queryOneValue(mapper, false, "SELECT COUNT(*) AS count FROM songs_unlocked WHERE id = ?", id)
    }

}
