package igrek.songbook.chords.render

import igrek.songbook.chords.model.LyricsTextType

open class TypefaceLengthMapper(
    private val normalCharLengths: HashMap<Char, Float> = hashMapOf(),
    private val boldCharLengths: HashMap<Char, Float> = hashMapOf(),
) {

    constructor(vararg pairs: Pair<Char, Float>) : this(
        HashMap(pairs.toMap()),
        HashMap(pairs.toMap())
    )

    fun put(type: LyricsTextType, char: Char, length: Float) {
        val charLengths: HashMap<Char, Float> = when (type) {
            LyricsTextType.CHORDS -> boldCharLengths
            else -> normalCharLengths
        }
        charLengths[char] = length
    }

    fun has(type: LyricsTextType, char: Char): Boolean {
        val charLengths: HashMap<Char, Float> = when (type) {
            LyricsTextType.CHORDS -> boldCharLengths
            else -> normalCharLengths
        }
        return char in charLengths
    }

    open fun charWidth(type: LyricsTextType, char: Char): Float {
        val charLengths: HashMap<Char, Float> = when (type) {
            LyricsTextType.CHORDS -> boldCharLengths
            else -> normalCharLengths
        }
        return charLengths[char] ?: 0f
    }

    fun stringWidth(type: LyricsTextType, text: String): Float {
        return text.toList().map { charWidth(type, it) }.sum()
    }

}