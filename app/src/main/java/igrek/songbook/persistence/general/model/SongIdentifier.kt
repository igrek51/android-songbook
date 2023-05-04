package igrek.songbook.persistence.general.model

data class SongIdentifier(
    var songId: String,
    var namespace: SongNamespace,
)
