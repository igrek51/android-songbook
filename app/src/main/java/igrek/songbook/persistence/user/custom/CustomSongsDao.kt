package igrek.songbook.persistence.user.custom

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import igrek.songbook.persistence.user.migrate.Migration037CustomSongs
import javax.inject.Inject

class CustomSongsDao(path: String) : AbstractJsonDao<CustomSongsDb>(
        path,
        dbName = "customsongs",
        schemaVersion = 2,
        clazz = CustomSongsDb::class.java,
        serializer = CustomSongsDb.serializer()
) {

    val customSongs: CustomSongsDb get() = db!!

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun empty(): CustomSongsDb {
        return CustomSongsDb(mutableListOf())
    }

    override fun migrateOlder(): CustomSongsDb? {
        return Migration037CustomSongs(activity).load()
    }

    fun saveCustomSong(newSong: CustomSong) {
        val olds = customSongs.songs
                .filter { song -> song.id != newSong.id }.toMutableList()
        if (newSong.id == 0L)
            newSong.id = nextId(olds)

        olds.add(newSong)
        customSongs.songs = olds
        songsRepository.reloadUserData()
    }

    private fun nextId(songs: MutableList<CustomSong>): Long {
        return (songs.map { song -> song.id }.max() ?: 0) + 1
    }

    fun removeCustomSong(newSong: CustomSong) {
        val olds = customSongs.songs
                .filter { song -> song.id != newSong.id }.toMutableList()
        customSongs.songs = olds
        songsRepository.reloadUserData()
    }

}