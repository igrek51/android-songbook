package igrek.songbook.persistence.user.exclusion

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import javax.inject.Inject

class ExclusionDao(path: String) : AbstractJsonDao<ExclusionDb>(
        path,
        dbName = "exclusion",
        schemaVersion = 2,
        clazz = ExclusionDb::class.java,
        serializer = ExclusionDb.serializer()
) {

    val exclusionDb: ExclusionDb get() = db!!

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
        read()
    }

    override fun empty(): ExclusionDb {
        return ExclusionDb()
    }

    fun setExcludedLanguages(languages: MutableList<String>) {
        exclusionDb.languages = languages
    }

}