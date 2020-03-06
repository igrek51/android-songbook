package igrek.songbook.persistence.general.model

enum class SongStatus(val id: Long) {

    PUBLISHED(1),

    PROPOSED(2),

    CUSTOM(3),

    APPROVED(4),

    ABANDONED(5),

    ;

    companion object {
        fun parseById(id: Long): SongStatus? {
            return values().firstOrNull { v -> v.id == id }
        }
    }
}
