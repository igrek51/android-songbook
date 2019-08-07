package igrek.songbook.persistence.user.playlist

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import javax.inject.Inject

class PlaylistDao(path: String) : AbstractJsonDao<PlaylistDb>(
        path,
        dbName = "unlocked",
        schemaVersion = 2,
        clazz = PlaylistDb::class.java,
        serializer = PlaylistDb.serializer()
) {

    val playlistDb: PlaylistDb get() = db!!

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun empty(): PlaylistDb {
        return PlaylistDb(mutableListOf())
    }

    fun savePlaylist(newPlaylist: Playlist) {
        val olds = playlistDb.playlists
                .filter { playlist -> playlist.id != newPlaylist.id }.toMutableList()
        if (newPlaylist.id == 0L)
            newPlaylist.id = nextId(olds)
        olds.add(newPlaylist)
        playlistDb.playlists = olds
    }

    fun removePlaylist(playlist: Playlist) {
        val olds = playlistDb.playlists
                .filter { p -> p.id != playlist.id }.toMutableList()
        playlistDb.playlists = olds
    }

    private fun nextId(playlists: List<Playlist>): Long {
        return (playlists.map { p -> p.id }.max() ?: 0) + 1
    }
}