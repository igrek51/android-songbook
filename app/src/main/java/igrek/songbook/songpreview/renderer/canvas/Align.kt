package igrek.songbook.songpreview.renderer.canvas

enum class Align(private val bits: Int) {

    LEFT(0x001),
    RIGHT(0x002),
    HCENTER(0x004),
    TOP(0x010),
    BOTTOM(0x020),
    VCENTER(0x040),

    CENTER(0x004 or 0x040),
    BOTTOM_LEFT(0x020 or 0x001),
    BOTTOM_RIGHT(0x020 or 0x002),
    BOTTOM_HCENTER(0x020 or 0x004),
    TOP_LEFT(0x010 or 0x001),
    TOP_RIGHT(0x010 or 0x002),
    TOP_HCENTER(0x010 or 0x004),
    ;

    fun isFlagSet(flag: Align): Boolean {
        return this.bits and flag.bits == flag.bits
    }
}