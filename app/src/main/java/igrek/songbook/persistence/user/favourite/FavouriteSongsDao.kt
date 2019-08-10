package igrek.songbook.persistence.user.favourite

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.WrapContextError
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import igrek.songbook.persistence.user.migrate.Migration037Favourites
import igrek.songbook.util.lookup.SimpleCache
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class FavouriteSongsDao(path: String) : AbstractJsonDao<FavouriteSongsDb>(
        path,
        dbName = "favourites",
        schemaVersion = 1,
        clazz = FavouriteSongsDb::class.java,
        serializer = FavouriteSongsDb.serializer()
) {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    val favouriteSongs: FavouriteSongsDb get() = db!!
    private var subscription: Disposable? = null

    private var favouritesCache: SimpleCache<HashSet<Song>> =
            SimpleCache {
                val favouriteSongs = songsRepository.songsDb!!.songs.filter { song ->
                    FavouriteSong(song.id, song.custom) in favouriteSongs.favourites
                }
                HashSet(favouriteSongs)
            }

    init {
        DaggerIoc.factoryComponent.inject(this)

        subscription?.dispose()
        subscription = songsRepository.dbChangeSubject.subscribe {
            favouritesCache.invalidate()
        }

        read()
    }

    override fun empty(): FavouriteSongsDb {
        return FavouriteSongsDb(mutableListOf())
    }

    override fun migrateOlder(): FavouriteSongsDb? {
        try {
            return Migration037Favourites(activity).load()
        } catch (t: Exception) {
            throw WrapContextError("Migration037Favourites error", t)
        }
    }

    fun isSongFavourite(songIdentifier: SongIdentifier): Boolean {
        val favSong = FavouriteSong(songIdentifier.songId, songIdentifier.custom)
        return favSong in favouriteSongs.favourites
    }

    fun getFavouriteSongs(): Set<Song> {
        return favouritesCache.get()
    }

    fun setSongFavourite(song: Song) {
        val favSong = FavouriteSong(song.id, song.custom)
        if (favSong !in favouriteSongs.favourites)
            favouriteSongs.favourites.add(favSong)
        favouritesCache.get().add(song)
    }

    fun unsetSongFavourite(song: Song) {
        val favSong = FavouriteSong(song.id, song.custom)
        if (favSong in favouriteSongs.favourites)
            favouriteSongs.favourites.remove(favSong)
        favouritesCache.get().remove(song)
    }

}