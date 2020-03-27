package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsTextType

data class TypefaceLengthMapper(
        val normalCharLengths: HashMap<Char, Float> = hashMapOf(),
        val boldCharLengths: HashMap<Char, Float> = hashMapOf()
) {

    constructor(vararg pairs: Pair<Char, Float>) : this(HashMap(pairs.toMap()), HashMap(pairs.toMap()))

    fun put(type: LyricsTextType, char: Char, length: Float) {
        val charLengths: HashMap<Char, Float> = when (type) {
            LyricsTextType.CHORDS -> boldCharLengths
            else -> normalCharLengths
        }
        charLengths[char] = length
    }

    fun get(type: LyricsTextType, char: Char): Float {
        val charLengths: HashMap<Char, Float> = when (type) {
            LyricsTextType.CHORDS -> boldCharLengths
            else -> normalCharLengths
        }
        return charLengths[char] ?: 0f
    }

}