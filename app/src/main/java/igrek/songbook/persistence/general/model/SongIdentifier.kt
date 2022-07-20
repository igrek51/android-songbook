package igrek.songbook.persistence.general.model

data class SongIdentifier(
    var songId: Long,
    var namespace: SongNamespace,
)
