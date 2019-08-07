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

}