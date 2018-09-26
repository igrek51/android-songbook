package igrek.songbook.persistence

import android.content.ContentValues
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
        if (isSongLocked(id))
            return
        // if song is not locked
        val db = getDbHelper().writableDatabase
        val values = ContentValues()
        values.put("id", id)
        db.insert("songs_unlocked", null, values)
    }

    fun isSongLocked(id: Long): Boolean {
        try {
            val cursor = sqlQuery("SELECT * FROM songs_unlocked WHERE id = ?", id)
            val count = cursor.count
            cursor.close()
            return count > 0
        } catch (e: IllegalArgumentException) {
            logger.error(e)
            return true
        }
    }

}
