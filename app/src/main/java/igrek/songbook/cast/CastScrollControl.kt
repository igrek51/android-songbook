package igrek.songbook.cast

import igrek.songbook.R

enum class CastScrollControl(
    val id: Long,
    val slideLines: Int,
    val descriptionResId: Int,
) {

    NONE(0, 0, R.string.songcast_scrollcontrol_none),
    SHARE_SCROLL(1, 0, R.string.songcast_scrollcontrol_scroll),
    SLIDES_1(2, 1, R.string.songcast_scrollcontrol_slides1),
    SLIDES_2(3, 2, R.string.songcast_scrollcontrol_slides2),
    SLIDES_4(4, 4, R.string.songcast_scrollcontrol_slides4),
    SLIDES_8(5, 8, R.string.songcast_scrollcontrol_slides8),
    ;

    companion object {
        val default = SHARE_SCROLL

        fun mustParseById(id: Long?): CastScrollControl {
            return CastScrollControl.values().firstOrNull { v -> v.id == id } ?: default
        }
    }
}