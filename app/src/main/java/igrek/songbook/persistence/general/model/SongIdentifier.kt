package igrek.songbook.persistence.general.model

data class SongIdentifier(
        var songId: Long,
        var custom: Boolean = false
)
