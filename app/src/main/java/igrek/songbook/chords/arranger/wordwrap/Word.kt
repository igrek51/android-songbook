package igrek.songbook.chords.arranger.wordwrap

import igrek.songbook.chords.model.LyricsTextType

data class Word(
    var text: String,
    val type: LyricsTextType,
    var x: Float = 0f,
    var width: Float = 0f,
) {
    override fun toString(): String = "($text,x=$x,w=$width)"

    val end: Float
        get() = x + width
}
