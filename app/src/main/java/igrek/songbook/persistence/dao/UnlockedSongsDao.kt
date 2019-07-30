package igrek.songbook.persistence.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import igrek.songbook.dagger.DaggerIoc


class UnlockedSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun getDatabase(): SQLiteDatabase {
        return localDbService.get().openLocalSongsDb()
    }

    fun readUnlockedKeys(): List<String> {
        val keys: MutableList<String> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM unlocked_keys")
            while (cursor.moveToNext()) {
                val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lock_password"))
                keys.add(lockPassword)
            }
            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return keys
    }

    fun unlockKey(key: String) {
        if (isSongUnlocked(key))
            return
        val values = ContentValues()
        values.put("lock_password", key)
        safeInsert("unlocked_keys", values)
    }

    private fun isSongUnlocked(key: String): Boolean {
        val mapper: (Cursor) -> Boolean = { cursor -> cursor.getLong(cursor.getColumnIndexOrThrow("count")) > 0 }
        return queryOneValue(mapper, false, "SELECT COUNT(*) AS count FROM unlocked_keys WHERE lock_password = ?", key)
    }

}
