package igrek.songbook.persistence.general.model

enum class SongNamespace(val id: Long) {

    Public(1),

    Custom(2),

    Antechamber(3),

    Ephemeral(4),

    ;

    companion object {
        fun parseById(id: Long): SongNamespace {
            return values().first { v -> v.id == id }
        }
    }
}
