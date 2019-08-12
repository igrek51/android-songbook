package igrek.songbook.persistence.user.exclusion

import kotlinx.serialization.Serializable

@Serializable
data class ExclusionDb(
        var languages: MutableList<String> = mutableListOf(),
        var artistIds: MutableList<Long> = mutableListOf()
)
