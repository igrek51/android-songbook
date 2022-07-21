package igrek.songbook.settings.enums

import igrek.songbook.R

enum class ChordsInstrument(val id: Long, val displayNameResId: Int) {

    GUITAR(1, R.string.instrument_guitar),

    UKULELE(2, R.string.instrument_ukulele),

    MANDOLIN(3, R.string.instrument_mandolin),

    PIANO(4, R.string.instrument_piano),

    ;

    companion object {
        val default: ChordsInstrument
            get() = GUITAR

        fun parseById(id: Long): ChordsInstrument? {
            return values().firstOrNull { v -> v.id == id }
        }
    }

}
