package igrek.songbook.persistence.user.custom

import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import java.util.Date

class CustomSongsDao(
    path: String,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    customSongsBackuper: LazyInject<CustomSongsBackuper> = appFactory.customSongsBackuper,
    resetOnError: Boolean = false,
) : AbstractJsonDao<CustomSongsDb>(
    path,
    dbName = "customsongs",
    schemaVersion = 1,
    clazz = CustomSongsDb::class.java,
    serializer = CustomSongsDb.serializer(),
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val customSongsBackuper by LazyExtractor(customSongsBackuper)

    val customSongs: CustomSongsDb get() = db!!
    var customCategories = listOf<CustomCategory>()

    init {
        read(resetOnError)
    }

    override fun empty(): CustomSongsDb {
        return CustomSongsDb(mutableListOf())
    }

    override fun save() {
        super.save()
        customSongsBackuper.saveBackup()
    }

    fun saveCustomSong(newSong: CustomSong): Song {
        if (newSong.id.isEmpty())
            newSong.id = DeviceIdProvider().newUUID()
        val index = customSongs.songs.indexOfFirst { it.id == newSong.id }
        if (index >= 0) {
            customSongs.songs[index] = newSong
        } else {
            customSongs.songs.add(newSong)
        }

        songsRepository.saveAndReloadUserSongs()
        val customModelSong = songsRepository.customSongsRepo.songFinder.find(
            SongIdentifier(
                newSong.id,
                SongNamespace.Custom
            )
        )
        return customModelSong!!
    }

    fun addNewCustomSongs(newSongs: List<CustomSong>): Int {
        var added = 0
        newSongs.forEach { newSong ->
            if (!customSongs.songs.any { it.title == newSong.title && it.categoryName == newSong.categoryName }) {
                newSong.id = DeviceIdProvider().newUUID()
                customSongs.songs.add(newSong)
                added++
            }
        }

        songsRepository.saveAndReloadUserSongs()
        return added
    }

    fun removeCustomSong(song: CustomSong) {
        val olds = customSongs.songs
            .filter { it.id != song.id }.toMutableList()
        customSongs.songs = olds

        // clean up other usages
        songsRepository.favouriteSongsDao.removeUsage(song.id, true)
        songsRepository.playlistDao.removeUsage(song.id, true)
        songsRepository.openHistoryDao.removeUsage(song.id, true)
        songsRepository.transposeDao.removeUsage(song.id, true)
        songsRepository.songTweakDao.removeUsage(SongIdentifier(song.id, SongNamespace.Custom))

        // remember timestamp of removal
        customSongs.syncSessionData.localTrash[song.id] = Date().time / 1000

        songsRepository.saveAndReloadUserSongs()
    }

}