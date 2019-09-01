package igrek.songbook.persistence.user.history

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class OpenHistoryDao(path: String) : AbstractJsonDao<OpenHistoryDb>(
        path,
        dbName = "history",
        schemaVersion = 1,
        clazz = OpenHistoryDb::class.java,
        serializer = OpenHistoryDb.serializer()
) {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    val historyDb: OpenHistoryDb get() = db!!
    val historyDbSubject = PublishSubject.create<OpenHistoryDb>()

    private val openedHistoryLimit = 50

    init {
        DaggerIoc.factoryComponent.inject(this)
        read()
    }

    override fun empty(): OpenHistoryDb {
        return OpenHistoryDb()
    }

    fun registerOpenedSong(songId: Long, custom: Boolean) {
        // remove other occurrences and old history
        historyDb.songs = historyDb.songs
                .filter { s -> !(s.songId == songId && s.custom == custom) }
                .take(openedHistoryLimit - 1)
                .toMutableList()
        historyDb.songs.add(0, OpenedSong(songId, custom, Date().time))
        historyDbSubject.onNext(historyDb)
    }

    fun removeUsage(songId: Long, custom: Boolean) {
        historyDb.songs = historyDb.songs.filter { song ->
            !(song.songId == songId && song.custom == custom)
        }.toMutableList()
    }
}