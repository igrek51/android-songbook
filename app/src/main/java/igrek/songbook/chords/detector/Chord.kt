package igrek.songbook.chords.detector


data class Chord(
        val noteIndex: Int,
        val minor: Boolean,
        val suffix: String = ""
)