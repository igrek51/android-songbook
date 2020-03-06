package igrek.songbook.persistence.user.exclusion

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryNameComparator
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class ExclusionDao(path: String) : AbstractJsonDao<ExclusionDb>(
        path,
        dbName = "exclusion",
        schemaVersion = 1,
        clazz = ExclusionDb::class.java,
        serializer = ExclusionDb.serializer()
) {

    val exclusionDb: ExclusionDb get() = db!!
    private val exclusionDbSubject = PublishSubject.create<ExclusionDb>()

    var allArtistsFilterEntries = LinkedHashMap<String, String>()

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
        exclusionDbSubject.onNext(exclusionDb)
        songsRepository.reloadSongsDb()
    }

    fun setExcludedArtists(artistIds: MutableList<Long>) {
        exclusionDb.artistIds = artistIds
        exclusionDbSubject.onNext(exclusionDb)
        songsRepository.reloadSongsDb()
    }

    fun setAllArtists(categories: List<Category>) {
        val map = LinkedHashMap<String, String>()
        categories.asSequence()
                .filter { it.type == CategoryType.ARTIST && it.displayName != null }
                .sortedWith(CategoryNameComparator)
                .forEach { category ->
                    map[category.id.toString()] = category.displayName!!
                }
        allArtistsFilterEntries = map
    }


}