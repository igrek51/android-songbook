package igrek.songbook.persistence.user.transpose

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class TransposeDao(path: String) : AbstractJsonDao<TransposeDb>(
        path,
        dbName = "transpose",
        schemaVersion = 1,
        clazz = TransposeDb::class.java,
        serializer = TransposeDb.serializer()
) {

    val transposeDb: TransposeDb get() = db!!
    val transposeDbSubject = PublishSubject.create<TransposeDb>()

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
        read()
    }

    override fun empty(): TransposeDb {
        return TransposeDb()
    }

}