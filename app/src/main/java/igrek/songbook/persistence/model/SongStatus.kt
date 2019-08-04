package igrek.songbook.persistence.model

enum class SongStatus(val id: Long) {

    PUBLISHED(1),

    PROPOSED(2);

    companion object {
        fun parseById(id: Long): SongStatus {
            return values().first { v -> v.id == id }
        }
    }
}
