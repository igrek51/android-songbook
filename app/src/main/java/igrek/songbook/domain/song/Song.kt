package igrek.songbook.domain.song

data class Song(
        val id: Long,
        var fileContent: String? = null,
        var title: String? = null,
        var categoryName: String? = null,
        var versionNumber: Long = 0,
        var updateTime: Long = 0,
        var custom: Boolean = false,
        var filename: String? = null,
        var comment: String? = null,
        var preferredKey: String? = null,
        var locked: Boolean = false,
        var lockPassword: String? = null
)
