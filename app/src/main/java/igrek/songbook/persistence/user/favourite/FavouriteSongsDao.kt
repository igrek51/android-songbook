package igrek.songbook.persistence.user.favourite

import android.annotation.SuppressLint
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import igrek.songbook.util.lookup.SimpleCache
import io.reactivex.android.schedulers.AndroidSchedulers

@SuppressLint("CheckResult")
class FavouriteSongsDao(
    path: String,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    resetOnError: Boolean = false,
) : AbstractJsonDao<FavouriteSongsDb>(
    path,
    dbName = "favourites",
    schemaVersion = 1,
    clazz = FavouriteSongsDb::class.java,
    serializer = FavouriteSongsDb.serializer()
) {
    private val songsRepository by LazyExtractor(songsRepository)

    private val favouriteSongs: FavouriteSongsDb get() = db!!

    private var favouritesCache: SimpleCache<HashSet<Song>> =
        SimpleCache {
            val favouriteSongs = this.songsRepository.allSongsRepo.songs.get().filter { song ->
                FavouriteSong(song.id, song.isCustom()) in favouriteSongs.favourites
            }
            HashSet(favouriteSongs)
        }

    init {
        this.songsRepository.dbChangeSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                favouritesCache.invalidate()
            }, UiErrorHandler::handleError)

        read(resetOnError)
    }

    override fun empty(): FavouriteSongsDb {
        return FavouriteSongsDb(mutableListOf())
    }

    fun isSongFavourite(songIdentifier: SongIdentifier): Boolean {
        val favSong = FavouriteSong(
            songIdentifier.songId,
            songIdentifier.namespace == SongNamespace.Custom
        )
        return favSong in favouriteSongs.favourites
    }

    fun getFavouriteSongs(): Set<Song> {
        return favouritesCache.get()
    }

    fun setSongFavourite(song: Song) {
        val favSong = FavouriteSong(song.id, song.isCustom())
        if (favSong !in favouriteSongs.favourites)
            favouriteSongs.favourites.add(favSong)
        favouritesCache.get().add(song)
    }

    fun unsetSongFavourite(song: Song) {
        val favSong = FavouriteSong(song.id, song.isCustom())
        if (favSong in favouriteSongs.favourites)
            favouriteSongs.favourites.remove(favSong)
        favouritesCache.get().remove(song)
    }

    fun removeUsage(songId: String, custom: Boolean) {
        val favSong = FavouriteSong(songId, custom)
        if (favSong in favouriteSongs.favourites)
            favouriteSongs.favourites.remove(favSong)
        favouritesCache.invalidate()
    }

}