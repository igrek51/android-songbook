package igrek.songbook.cast

enum class CastFocusControl(val id: Long, val desctiption: String) {
    NONE(0, "No focus"),
    SHARE_SCROLL(1, "Share your scroll"),
    SLIDES_1(2, "Slides mode: 1 line"),
    SLIDES_2(3, "Slides mode: 2 lines"),
    SLIDES_4(4, "Slides mode: 4 lines"),
    ;

    companion object {
        val default = SHARE_SCROLL

        fun mustParseById(id: Long?): CastFocusControl {
            return CastFocusControl.values().firstOrNull { v -> v.id == id } ?: default
        }
    }
}