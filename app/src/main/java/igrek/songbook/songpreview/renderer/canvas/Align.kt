package igrek.songbook.songpreview.renderer.canvas

object Align {
    val DEFAULT = 0x000
    //  Pozycja
    val LEFT = 0x001
    val RIGHT = 0x002
    val HCENTER = 0x004
    val TOP = 0x010
    val BOTTOM = 0x020
    val VCENTER = 0x040
    // mieszane
    val CENTER = HCENTER or VCENTER
    val BOTTOM_LEFT = BOTTOM or LEFT
    val BOTTOM_RIGHT = BOTTOM or RIGHT
    val BOTTOM_HCENTER = BOTTOM or HCENTER
    val TOP_LEFT = TOP or LEFT
    val TOP_RIGHT = TOP or RIGHT
    val TOP_HCENTER = TOP or HCENTER
    //  Rozmiar
    val HADJUST = 0x100
    val VADJUST = 0x200
    val ADJUST = HADJUST or VADJUST
}