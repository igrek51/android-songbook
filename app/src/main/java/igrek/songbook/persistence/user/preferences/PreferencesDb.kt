package igrek.songbook.persistence.user.preferences

import kotlinx.serialization.Serializable

@Serializable
data class PreferencesDb(
    var entries: MutableSet<PreferenceEntry> = mutableSetOf()
)

@Serializable
data class PreferenceEntry(
    val name: String,
    val value: PreferenceValue
)

@Serializable
data class PreferenceValue(
    val stringValue: String? = null,
    val longValue: Long? = null,
    val floatValue: Float? = null,
    val booleanValue: Boolean? = null
)