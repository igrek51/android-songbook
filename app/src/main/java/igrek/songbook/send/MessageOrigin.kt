package igrek.songbook.send

enum class MessageOrigin(val id: Long) {

    CONTACT_MESSAGE(1),

    SONG_PUBLISH(2),

    MISSING_SONG(3),

    ;
}