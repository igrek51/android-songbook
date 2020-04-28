package igrek.songbook.persistence.user.custom

import android.app.Activity
import igrek.songbook.info.logger.WrapContextError
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import igrek.songbook.persistence.user.migrate.Migration037CustomSongs

class CustomSongsDao(
        path: String,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        activity: LazyInject<Activity> = appFactory.activity,
) : AbstractJsonDao<CustomSongsDb>(
        path,
        dbName = "customsongs",
        schemaVersion = 1,
        clazz = CustomSongsDb::class.java,
        serializer = CustomSongsDb.serializer()
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val activity by LazyExtractor(activity)

    val customSongs: CustomSongsDb get() = db!!
    var customCategories = listOf<CustomCategory>()

    init {
        read()
    }

    override fun empty(): CustomSongsDb {
        return CustomSongsDb(mutableListOf())
    }

    override fun migrateOlder(): CustomSongsDb? {
        try {
            return Migration037CustomSongs(activity).load()
        } catch (t: Throwable) {
            throw WrapContextError("Migration037CustomSongs error", t)
        }
    }

    fun saveCustomSong(newSong: CustomSong): Song {
        val olds = customSongs.songs.filter {
            song -> song.id != newSong.id
        }.toMutableList()
        if (newSong.id == 0L)
            newSong.id = nextId(olds)

        olds.add(newSong)
        customSongs.songs = olds
        songsRepository.reloadUserData()
        val customModelSong = songsRepository.customSongsRepo.songFinder.find(SongIdentifier(newSong.id, SongNamespace.Custom))
        return customModelSong!!
    }

    private fun nextId(songs: MutableList<CustomSong>): Long {
        return (songs.map { song -> song.id }.max() ?: 0) + 1
    }

    fun removeCustomSong(newSong: CustomSong) {
        val olds = customSongs.songs
                .filter { song -> song.id != newSong.id }.toMutableList()
        customSongs.songs = olds
        // clean up other usages
        songsRepository.favouriteSongsDao.removeUsage(newSong.id, true)
        songsRepository.playlistDao.removeUsage(newSong.id, true)
        songsRepository.openHistoryDao.removeUsage(newSong.id, true)
        songsRepository.transposeDao.removeUsage(newSong.id, true)

        songsRepository.reloadUserData()
    }

}