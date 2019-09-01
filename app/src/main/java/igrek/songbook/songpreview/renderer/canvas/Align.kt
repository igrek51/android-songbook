package igrek.songbook.songpreview.renderer.canvas

object Align {
    // position
    const val LEFT = 0x001
    const val RIGHT = 0x002
    const val HCENTER = 0x004
    const val TOP = 0x010
    const val BOTTOM = 0x020
    const val VCENTER = 0x040
    // mixed
    const val CENTER = HCENTER or VCENTER
    const val BOTTOM_LEFT = BOTTOM or LEFT
    const val BOTTOM_RIGHT = BOTTOM or RIGHT
    const val BOTTOM_HCENTER = BOTTOM or HCENTER
    const val TOP_LEFT = TOP or LEFT
    const val TOP_RIGHT = TOP or RIGHT
    const val TOP_HCENTER = TOP or HCENTER
}