package igrek.songbook.persistence.general.model

enum class SongStatus(val id: Long) {

    PUBLISHED(1),

    PROPOSED(2),

    CUSTOM(3),

    ACCEPTED(3);

    companion object {
        fun parseById(id: Long): SongStatus {
            return values().first { v -> v.id == id }
        }
    }
}
