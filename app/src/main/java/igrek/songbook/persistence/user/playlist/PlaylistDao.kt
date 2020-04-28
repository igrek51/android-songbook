package igrek.songbook.persistence.user.playlist

import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import io.reactivex.subjects.PublishSubject

class PlaylistDao(
        path: String,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) : AbstractJsonDao<PlaylistDb>(
        path,
        dbName = "playlist",
        schemaVersion = 1,
        clazz = PlaylistDb::class.java,
        serializer = PlaylistDb.serializer()
) {
    private val songsRepository by LazyExtractor(songsRepository)

    val playlistDb: PlaylistDb get() = db!!
    val playlistDbSubject = PublishSubject.create<PlaylistDb>()

    init {
        read()
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
        playlistDbSubject.onNext(playlistDb)
    }

    fun removePlaylist(playlist: Playlist) {
        val olds = playlistDb.playlists
                .filter { p -> p.id != playlist.id }.toMutableList()
        playlistDb.playlists = olds
        playlistDbSubject.onNext(playlistDb)
    }

    private fun nextId(playlists: List<Playlist>): Long {
        return (playlists.map { p -> p.id }.max() ?: 0) + 1
    }

    fun isSongOnPlaylist(song: Song, playlist: Playlist): Boolean {
        val playlistSong = PlaylistSong(song.id, song.isCustom())
        return playlistSong in playlist.songs
    }

    fun isSongOnAnyPlaylist(song: Song): Boolean {
        return songsRepository.playlistDao.playlistDb.playlists
                .any { playlist -> isSongOnPlaylist(song, playlist) }
    }

    fun addSongToPlaylist(song: Song, playlist: Playlist) {
        val playlistSong = PlaylistSong(song.id, song.isCustom())
        playlist.songs.add(playlistSong)
        playlistDbSubject.onNext(playlistDb)
    }

    fun removeSongFromPlaylist(song: Song, playlist: Playlist) {
        val playlistSong = PlaylistSong(song.id, song.isCustom())
        playlist.songs.remove(playlistSong)
        playlistDbSubject.onNext(playlistDb)
    }

    fun removeUsage(songId: Long, custom: Boolean) {
        val playlistSong = PlaylistSong(songId, custom)
        var removed = 0
        playlistDb.playlists.forEach { playlist ->
            if (playlist.songs.remove(playlistSong))
                removed++
        }
        if (removed > 0)
            playlistDbSubject.onNext(playlistDb)
    }
}