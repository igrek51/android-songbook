package igrek.songbook.cast

enum class CastScrollControl(
    val id: Long,
    val desctiption: String,
    val slideLines: Int,
) {

    NONE(0, "None", 0),
    SHARE_SCROLL(1, "Share your scroll", 0),
    SLIDES_1(2, "Slides mode: 1 line", 1),
    SLIDES_2(3, "Slides mode: 2 lines", 2),
    SLIDES_4(4, "Slides mode: 4 lines", 4),
    ;

    companion object {
        val default = SHARE_SCROLL

        fun mustParseById(id: Long?): CastScrollControl {
            return CastScrollControl.values().firstOrNull { v -> v.id == id } ?: default
        }
    }
}