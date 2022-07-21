package igrek.songbook.chordsv2.model

import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.settings.chordsnotation.ChordsNotation

// Single chord with one single note: Cm, F#maj7, Bbm
data class Chord (
    var displayText: String,

    /* Note index:
     0 - C
     1 - C# / Db
     2 - D
     3 - D# / Eb
     4 - E
     5 - F
     6 - F#
     7 - G
     8 - G#
     9 - A
     10 - A# / Bb
     11 - B
     */
    val noteIndex: Int,
    val minor: Boolean,
    val suffix: String,
    val originalNoteIndex: Int,
    val originalText: String,
) {
    constructor(noteIndex: Int, minor: Boolean, suffix: String = "") : this("", noteIndex, minor, suffix, originalNoteIndex=noteIndex, originalText="")

    fun render(notation: ChordsNotation, key: Int? = null): Chord {
        displayText = format(notation, key)
        return this
    }

    fun format(notation: ChordsNotation, key: Int? = null): String {
        if (noteIndex == originalNoteIndex)
            return originalText

        return when (notation) {
            ChordsNotation.GERMAN -> TODO()
            ChordsNotation.GERMAN_IS -> TODO()
            ChordsNotation.ENGLISH -> TODO()
            ChordsNotation.SOLFEGE -> TODO()
        }
    }

}
