package igrek.songbook.persistence.dao

import android.database.sqlite.SQLiteDatabase
import igrek.songbook.persistence.dao.mapper.CategoryMapper
import igrek.songbook.persistence.dao.mapper.SongCategoryMapper
import igrek.songbook.persistence.dao.mapper.SongMapper
import igrek.songbook.persistence.model.Category
import igrek.songbook.persistence.model.Song
import igrek.songbook.persistence.model.SongCategoryRelationship


class SongsDao(database: SQLiteDatabase) : AbstractSqliteDao(database) {

    /*
    SCHEMA:

class Category(models.Model):

    TYPE_ID_CHOICE = (
        (1, 'CUSTOM'),
        (2, 'OTHERS'),
        (3, 'ARTIST'),
    )

    type_id = models.IntegerField(default=3, choices=TYPE_ID_CHOICE)
    name = models.CharField(blank=True, null=True, max_length=512)


class Song(models.Model):

    STATE_ID_CHOICE = (
        (1, 'PUBLISHED'),
        (2, 'PROPOSED'),
    )

    LANGUAGE_CHOICES = (
        ('pl', 'pl'),
        ('en', 'en'),
    )

    CHORDS_NOTATION_CHOICES = (
        (1, 'GERMAN'),
        (2, 'GERMAN_IS'),
        (3, 'ENGLISH'),
    )

    title = models.CharField(max_length=512)
    categories = models.ManyToManyField(Category)
    content = models.TextField(blank=True, null=True)
    version_number = models.IntegerField(default=1)
    create_time = models.DateTimeField(default=datetime.now)
    update_time = models.DateTimeField(default=datetime.now)
    language = models.CharField(blank=True, null=True, max_length=8, choices=LANGUAGE_CHOICES)
    chords_notation = models.IntegerField(blank=True, null=True, default=1, choices=CHORDS_NOTATION_CHOICES)
    author = models.CharField(blank=True, null=True, max_length=512)
    preferred_key = models.CharField(blank=True, null=True, max_length=128)
    metre = models.CharField(blank=True, null=True, max_length=128)
    comment = models.TextField(blank=True, null=True)
    is_locked = models.BooleanField(default=False)
    lock_password = models.CharField(blank=True, null=True, max_length=512)
    scroll_speed = models.DecimalField(max_digits=8, decimal_places=4, blank=True, null=True)
    initial_delay = models.DecimalField(max_digits=10, decimal_places=3, blank=True, null=True)
    state = models.IntegerField(default=1, choices=STATE_ID_CHOICE)
    rank = models.DecimalField(max_digits=6, decimal_places=3, blank=True, null=True)
    tags = models.CharField(blank=True, null=True, max_length=512)


class Info(models.Model):
    name = models.CharField(max_length=512)
    value = models.CharField(blank=True, null=True, max_length=512)
     */

    private val songMapper = SongMapper()
    private val categoryMapper = CategoryMapper()
    private val songCategoryMapper = SongCategoryMapper()

    fun readAllCategories(): List<Category> {
        return readEntities("SELECT * FROM songs_category", categoryMapper)
    }

    fun readAllSongs(): MutableList<Song> {
        return readEntities("SELECT * FROM songs_song", songMapper)
    }

    fun readAllSongCategories(): List<SongCategoryRelationship> {
        return readEntities("SELECT * FROM songs_song_categories", songCategoryMapper)
    }

}
