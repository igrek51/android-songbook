package igrek.songbook.persistence.general.model

data class SongCategoryRelationship(
    var song_id: String,
    var category_id: Long,
)