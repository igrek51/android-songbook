package igrek.songbook.cast

enum class CastFocusControl(
    val id: Long,
    val desctiption: String,
    val slide: Boolean,
) {

    NONE(0, "None", false),
    SHARE_SCROLL(1, "Share your scroll", false),
    SLIDES_1(2, "Slides mode: 1 line", true),
    SLIDES_2(3, "Slides mode: 2 lines", true),
    SLIDES_4(4, "Slides mode: 4 lines", true),
    ;

    companion object {
        val default = SHARE_SCROLL

        fun mustParseById(id: Long?): CastFocusControl {
            return CastFocusControl.values().firstOrNull { v -> v.id == id } ?: default
        }
    }
}