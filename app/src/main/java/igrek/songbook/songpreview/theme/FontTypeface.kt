package igrek.songbook.songpreview.theme

import igrek.songbook.R

enum class FontTypeface(val id: String, val displayNameResId: Int) {

    SANS_SERIF("sans", R.string.typeface_sans_serif),

    SERIF("serif", R.string.typeface_serif),

    MONOSPACE("monospace", R.string.typeface_monospace);

    companion object {
        fun parseById(id: String): FontTypeface? {
            return FontTypeface.values().firstOrNull { v -> v.id == id }
        }
    }
}
