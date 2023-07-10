package igrek.songbook.persistence.user.preferences

import igrek.songbook.persistence.user.AbstractJsonDao

class PreferencesDao(
    path: String,
    resetOnError: Boolean = false,
) : AbstractJsonDao<PreferencesDb>(
    path,
    dbName = "preferences",
    schemaVersion = 1,
    clazz = PreferencesDb::class.java,
    serializer = PreferencesDb.serializer()
) {
    private val preferencesDb: PreferencesDb get() = db!!

    init {
        read(resetOnError)
    }

    override fun empty(): PreferencesDb {
        return PreferencesDb()
    }

    fun getPrimitiveEntries(): TypedPrimitiveEntries {
        val typedPrimitives = TypedPrimitiveEntries()

        preferencesDb.entries.forEach { entry: PreferenceEntry ->
            val value: PreferenceValue = entry.value
            when {
                value.stringValue != null -> typedPrimitives.stringValues[entry.name] = value.stringValue
                value.longValue != null -> typedPrimitives.longValues[entry.name] = value.longValue
                value.floatValue != null -> typedPrimitives.floatValues[entry.name] = value.floatValue
                value.booleanValue != null -> typedPrimitives.booleanValues[entry.name] = value.booleanValue
                else -> throw IllegalArgumentException("preference ${entry.name} has no value")
            }
        }
        return typedPrimitives
    }

    fun setPrimitiveEntries(entries: TypedPrimitiveEntries) {
        val stringEntries = entries.stringValues.map { (name, value) -> PreferenceEntry(name = name, value = PreferenceValue(stringValue = value)) }
        val longEntries = entries.longValues.map { (name, value) -> PreferenceEntry(name = name, value = PreferenceValue(longValue = value)) }
        val floatEntries = entries.floatValues.map { (name, value) -> PreferenceEntry(name = name, value = PreferenceValue(floatValue = value)) }
        val booleanEntries = entries.booleanValues.map { (name, value) -> PreferenceEntry(name = name, value = PreferenceValue(booleanValue = value)) }
        preferencesDb.entries = (stringEntries + longEntries + floatEntries + booleanEntries).toMutableSet()
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
        val entryValue = when (value) {
            is String -> PreferenceValue(stringValue = value)
            is Long -> PreferenceValue(longValue = value)
            is Float -> PreferenceValue(floatValue = value)
            is Boolean -> PreferenceValue(booleanValue = value)
            else -> throw IllegalArgumentException("cant set preference $name with unknown type value $value, ${value::class.simpleName}")
        }
        return PreferenceEntry(name = name, value = entryValue)
    }

}

data class TypedPrimitiveEntries(
    val stringValues: MutableMap<String, String> = mutableMapOf(),
    val longValues: MutableMap<String, Long> = mutableMapOf(),
    val floatValues: MutableMap<String, Float> = mutableMapOf(),
    val booleanValues: MutableMap<String, Boolean> = mutableMapOf(),
) {
    fun isEmpty(): Boolean = stringValues.isEmpty() && longValues.isEmpty() && floatValues.isEmpty() && booleanValues.isEmpty()

    fun get(prefName: String): Any? {
        return stringValues[prefName] ?: longValues[prefName] ?: floatValues[prefName] ?: booleanValues[prefName]
    }

    fun set(name: String, value: Any) {
        when (value) {
            is String -> this.stringValues[name] = value
            is Long -> this.longValues[name] = value
            is Float -> this.floatValues[name] = value
            is Boolean -> this.booleanValues[name] = value
            else -> throw IllegalArgumentException("cant set preference $name with unknown type value $value, ${value::class.simpleName}")
        }
    }
}