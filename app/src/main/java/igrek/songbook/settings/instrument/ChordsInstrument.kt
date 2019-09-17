package igrek.songbook.settings.instrument

import igrek.songbook.R

enum class ChordsInstrument(val id: Long, val displayNameResId: Int) {

    GUITAR(1, R.string.instrument_guitar),

    UKULELE(2, R.string.instrument_ukulele),

    MANDOLIN(3, R.string.instrument_mandolin),

    ;

    companion object {
        val default: ChordsInstrument
            get() = GUITAR

        fun parseById(id: Long?): ChordsInstrument? {
            if (id == null)
                return null
            return values().firstOrNull { v -> v.id == id }
        }
    }

}
