package igrek.songbook.songpreview.theme

import android.graphics.Typeface
import igrek.songbook.R

enum class FontTypeface(val id: String, val displayNameResId: Int, val typeface: Typeface) {

    SANS_SERIF("sans", R.string.typeface_sans_serif, Typeface.SANS_SERIF),

    SERIF("serif", R.string.typeface_serif, Typeface.SERIF),

    MONOSPACE("monospace", R.string.typeface_monospace, Typeface.MONOSPACE);

    companion object {
        fun parseById(id: String): FontTypeface? {
            return FontTypeface.values().firstOrNull { v -> v.id == id }
        }
    }
}
