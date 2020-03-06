package igrek.songbook.persistence.user.preferences

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import javax.inject.Inject

class PreferencesDao(path: String) : AbstractJsonDao<PreferencesDb>(
        path,
        dbName = "preferences",
        schemaVersion = 1,
        clazz = PreferencesDb::class.java,
        serializer = PreferencesDb.serializer()
) {

    private val preferencesDb: PreferencesDb get() = db!!

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
        read()
    }

    override fun empty(): PreferencesDb {
        return PreferencesDb()
    }

    fun getPrimitiveEntries(): Map<String, Any> {
        return preferencesDb.entries
                .map { entry -> entry.name to readEntryValue(entry) }
                .toMap()
    }

    fun setPrimitiveEntries(entries: Map<String, Any>) {
        preferencesDb.entries = entries
                .map { (name, value) -> buildEntryValue(name, value) }
                .toMutableSet()
    }

    fun setPrimitiveEntry(preferenceName: String, value: Any) {
        preferencesDb.entries.removeAll { entry -> entry.name == preferenceName }
        preferencesDb.entries.add(buildEntryValue(preferenceName, value))
    }

    private fun readEntryValue(entry: PreferenceEntry): Any {
        val value = entry.value
        return when {
            value.stringValue != null -> value.stringValue
            value.longValue != null -> value.longValue
            value.floatValue != null -> value.floatValue
            value.booleanValue != null -> value.booleanValue
            else -> throw IllegalArgumentException("preference ${entry.name} has no value")
        }
    }

    private fun buildEntryValue(name: String, value: Any): PreferenceEntry {
        val value = when (value) {
            is String -> PreferenceValue(stringValue = value)
            is Long -> PreferenceValue(longValue = value)
            is Float -> PreferenceValue(floatValue = value)
            is Boolean -> PreferenceValue(booleanValue = value)
            else -> throw IllegalArgumentException("cant set preference $name with unknown type value $value, ${value::class.simpleName}")
        }
        return PreferenceEntry(name = name, value = value)
    }

}