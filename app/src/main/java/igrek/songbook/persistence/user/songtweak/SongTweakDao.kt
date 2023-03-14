package igrek.songbook.persistence.user.songtweak

import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.user.AbstractJsonDao
import io.reactivex.subjects.PublishSubject

class SongTweakDao(
    path: String,
    resetOnError: Boolean = false,
) : AbstractJsonDao<SongTweakDb>(
    path,
    dbName = "songtweak",
    schemaVersion = 1,
    clazz = SongTweakDb::class.java,
    serializer = SongTweakDb.serializer()
) {
    private val songtweakDb: SongTweakDb get() = db!!
    private val songtweakDbSubject = PublishSubject.create<SongTweakDb>()

    init {
        read(resetOnError)
    }

    override fun empty(): SongTweakDb {
        return SongTweakDb()
    }

    fun getSongAutoscrollSpeed(songIdentifier: SongIdentifier): Float? {
        val songFound = songtweakDb.songs
            .find {
                it.songId == songIdentifier.songId
                        && it.namespaceId == songIdentifier.namespace.id
            }
        return songFound?.autoscrollSpeed
    }

    fun setSongAutoscrollSpeed(songIdentifier: SongIdentifier, autoscrollSpeed: Float) {
        val existingEntry = songtweakDb.songs
            .find {
                it.songId == songIdentifier.songId
                        && it.namespaceId == songIdentifier.namespace.id
            }
        if (existingEntry == null) {
            val newEntry = TweakedSong(
                songId = songIdentifier.songId,
                namespaceId = songIdentifier.namespace.id,
                autoscrollSpeed = autoscrollSpeed,
            )
            songtweakDb.songs.add(newEntry)
        } else {
            existingEntry.autoscrollSpeed = autoscrollSpeed
        }
        songtweakDbSubject.onNext(songtweakDb)
    }

    fun removeUsage(songIdentifier: SongIdentifier) {
        val songId = songIdentifier.songId
        val namespaceId = songIdentifier.namespace.id
        val songFound = songtweakDb.songs
            .find {
                it.songId == songId
                        && it.namespaceId == namespaceId
            }
        if (songFound != null) {
            songtweakDb.songs.remove(songFound)
            songtweakDbSubject.onNext(songtweakDb)
        }
    }

}