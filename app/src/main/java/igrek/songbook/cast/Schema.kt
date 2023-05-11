package igrek.songbook.cast

import kotlinx.serialization.Serializable

@Serializable
data class CastSessionJoin(
    var member_name: String,
)

@Serializable
data class CastSessionJoined(
    var short_id: String,
    var public_member_id: String,
    var member_name: String,
    var rejoined: Boolean,
)

@Serializable
data class CastSession(
    var short_id: String,
    var create_timestamp: Long, // in seconds
    var update_timestamp: Long, // in seconds
    var ttl: Long, // in seconds
    var members: List<CastMember>,
    var song: CastSong?,
    var scroll: CastScroll?,
    var chat_messages: List<CastChatMessage>,
)

@Serializable
data class CastMember(
    var public_member_id: String,
    var name: String,
    var type: String,
)

@Serializable
data class CastSong(
    var id: String,
    var chosen_by: String, // public member ID
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
)

@Serializable
data class CastScroll(
    var view_start: Float,
    var view_end: Float,
    var visible_text: String?,
)

@Serializable
data class CastChatMessage(
    var timestamp: Long, // in seconds
    var author: String,
    var text: String,
)

@Serializable
data class CastSongSelected(
    var id: String,
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
)

@Serializable
data class CastChatMessageSent(
    var text: String,
)

enum class CastMemberType(val value: String) {
    OWNER("owner"), // can pick current song, presenter
    GUEST("guest"), // read-only spectator
    ;
}
