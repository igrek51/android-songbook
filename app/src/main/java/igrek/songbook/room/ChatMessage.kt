package igrek.songbook.room

import java.util.Date

data class ChatMessage(
    val author: String,
    val message: String,
    val time: Date,
)