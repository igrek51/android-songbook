package igrek.songbook.settings.buttons

import igrek.songbook.R

enum class MediaButtonBehaviours(val id: Long, val nameResId: Int) {

    SCROLL_DOWN_NEXT_SONG(1, R.string.media_button_behaviour_scroll_down_and_next),

    SCROLL_DOWN(2, R.string.media_button_behaviour_scroll_down),

    NEXT_SONG(3, R.string.media_button_behaviour_next_song),

    DO_NOTHING(4, R.string.media_button_behaviour_nothing),

    ;

    companion object {
        val default = SCROLL_DOWN_NEXT_SONG

        fun parseById(id: Long): MediaButtonBehaviours? {
            return values().firstOrNull { v -> v.id == id }
        }

        fun mustParseById(id: Long): MediaButtonBehaviours {
            return parseById(id) ?: default
        }
    }
}
