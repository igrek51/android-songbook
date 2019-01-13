package igrek.songbook.songpreview.theme

import igrek.songbook.R

enum class ColorScheme(val id: Long, val displayNameResId: Int) {

    DARK(1, R.string.color_scheme_dark),

    BRIGHT(2, R.string.color_scheme_bright);

    companion object {
        fun parseById(id: Long): ColorScheme? {
            return ColorScheme.values().firstOrNull { v -> v.id == id }
        }
    }
}
