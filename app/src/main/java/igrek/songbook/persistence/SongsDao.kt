package igrek.songbook.persistence

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongCategoryType
import igrek.songbook.domain.songsdb.SongStatus


class SongsDao : AbstractSqliteDao() {

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
        return localDbService.openSongsDb()
    }

    fun readAllCategories(): List<SongCategory> {
        val entities: MutableList<SongCategory> = mutableListOf()
        try {
            val cursor = sqlQuery("SELECT * FROM songs_category ORDER BY id")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val typeId = cursor.getLong(cursor.getColumnIndexOrThrow("type_id"))
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

                val song = Song(id, title, category, fileContent, versionNumber, updateTime, custom, filename, comment, preferredKey, locked, lockPassword, author, songStatus)
                songs.add(song)
            }

            cursor.close()
        } catch (e: IllegalArgumentException) {
            logger.error(e)
        }
        return songs
    }

}
