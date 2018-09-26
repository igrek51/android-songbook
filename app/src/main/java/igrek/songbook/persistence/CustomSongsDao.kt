package igrek.songbook.persistence

import android.content.ContentValues
import android.database.Cursor
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongCategoryType
import igrek.songbook.domain.songsdb.SongStatus
import java.util.*


class CustomSongsDao : AbstractSqliteDao() {

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    /*
    SCHEMA:

class Category(models.Model):
    type_id = models.IntegerField()
    name = models.CharField(blank=True, null=True, max_length=512)

class Song(models.Model):
    title = models.CharField(max_length=512)
    category = models.ForeignKey(Category, on_delete=models.CASCADE)
    file_content = models.TextField(blank=True, null=True)
    version_number = models.IntegerField()
    create_time = models.DateTimeField()
    update_time = models.DateTimeField()
    is_custom = models.BooleanField(default=False)
    filename = models.CharField(blank=True, null=True, max_length=512)
    comment = models.CharField(blank=True, null=True, max_length=512)
    preferred_key = models.CharField(blank=True, null=True, max_length=512)
    is_locked = models.BooleanField(default=False)
    lock_password = models.CharField(blank=True, null=True, max_length=512)
    author = models.CharField(blank=True, null=True, max_length=512)
    state = models.IntegerField(default=1)

class Info(models.Model):
    name = models.CharField(max_length=512)
    value = models.CharField(blank=True, null=True, max_length=512)

     */

    override fun getDbHelper(): SQLiteDbHelper {
        return localDbService.openCustomSongsDb()
    }

    fun readAllCategories(): List<SongCategory> {
        val entities: MutableList<SongCategory> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_category ORDER BY id")

            while (cursor.moveToNext()) {
                entities.add(mapSongCategory(cursor))
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return entities
    }

    fun mapSongCategory(cursor: Cursor): SongCategory {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val typeId = cursor.getLong(cursor.getColumnIndexOrThrow("type_id"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val type = SongCategoryType.parseById(typeId)
        return SongCategory(id, type, name)
    }

    fun readAllSongs(categories: List<SongCategory>): MutableList<Song> {
        val songs: MutableList<Song> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_song ORDER BY id")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val fileContent = cursor.getString(cursor.getColumnIndexOrThrow("file_content"))
                val versionNumber = cursor.getLong(cursor.getColumnIndexOrThrow("version_number"))
                // TODO get date from datetime column type
                val createTime = getTimestampColumn(cursor, "create_time")
                val updateTime = getTimestampColumn(cursor, "update_time")
                val custom = getBooleanColumn(cursor, "is_custom")
                val filename = cursor.getString(cursor.getColumnIndexOrThrow("filename"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val preferredKey = cursor.getString(cursor.getColumnIndexOrThrow("preferred_key"))
                val locked = getBooleanColumn(cursor, "is_locked")
                val lockPassword = cursor.getString(cursor.getColumnIndexOrThrow("lock_password"))
                val author = cursor.getString(cursor.getColumnIndexOrThrow("author"))
                val stateId = cursor.getLong(cursor.getColumnIndexOrThrow("state"))
                val categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))

                val songStatus = SongStatus.parseById(stateId)
                val category = categories.first { category -> category.id == categoryId }

                val song = Song(id, title, category, fileContent, versionNumber, createTime, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, songStatus)
                songs.add(song)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songs
    }

    private fun getNextSongId(): Long {
        val mapper: (Cursor) -> Long = { cursor -> cursor.getLong(cursor.getColumnIndexOrThrow("max")) }
        val maxId: Long = queryOneValue(mapper, 0, "SELECT MAX(id) AS max FROM songs_song")
        return maxId + 1
    }

    fun getCategoryByTypeId(categoryTypeId: Long): SongCategory? {
        val mapper: (Cursor) -> SongCategory = { cursor -> mapSongCategory(cursor) }
        return queryOneValue(mapper, null, "SELECT * FROM songs_category WHERE type_id = ?", categoryTypeId)
    }

    fun saveCustomSong(song: Song) {
        // auto increment id
        song.id = getNextSongId()
        // insert new song
        val db = getDbHelper().writableDatabase
        val values = ContentValues()
        values.put("id", song.id)
        values.put("title", song.title)
        values.put("category_id", song.category.id)
        values.put("file_content", song.fileContent)
        values.put("version_number", song.versionNumber)
        values.put("create_time", iso8601Format.format(Date(song.createTime)))
        values.put("update_time", iso8601Format.format(Date(song.updateTime)))
        values.put("is_custom", song.custom)
        values.put("filename", song.filename)
        values.put("comment", song.comment)
        values.put("preferred_key", song.preferredKey)
        values.put("is_locked", song.locked)
        values.put("lock_password", song.lockPassword)
        values.put("author", song.author)
        values.put("state", song.status.id)
        db.insert("songs_song", null, values)
    }

}
