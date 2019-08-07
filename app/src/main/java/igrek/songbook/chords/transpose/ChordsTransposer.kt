package igrek.songbook.chords.transpose

import igrek.songbook.chords.ChordNameProvider
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.splitter.ChordsSplitter
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.lookup.SimpleCache

class ChordsTransposer(private val chordsNotation: ChordsNotation) {

    private val chordNameProvider = ChordNameProvider()
    private val chordsSplitter = ChordsSplitter()
    private val defaultChordsDetector = ChordsDetector(ChordsNotation.default)

    private val chordsNotationSoundNames: SimpleCache<List<String>> = SimpleCache {
        chordNameProvider.allChords(chordsNotation)
    }

    /**
     * @param input file content with lyrics and chords
     * @param t  shift semitones count
     * @return content transposed by semitones (the same format as input)
     */
    fun transposeContent(input: String, t: Int): String {
        val out = StringBuilder()
        val chordsSection = StringBuilder()
        var bracket = false

        for (i in 0 until input.length) {
            val c = input[i]
            if (c == '[') {
                bracket = true
                out.append(c)
            } else if (c == ']') {
                bracket = false
                // transpose whole chords section
                val transposedChords = transposeChords(chordsSection.toString(), t)
                out.append(transposedChords)
                out.append(c)
                chordsSection.delete(0, chordsSection.length)
            } else { // the regular character
                if (bracket) {
                    chordsSection.append(c) // move to chords section buffer
                } else {
                    out.append(c)
                }
            }
        }

        return out.toString()
    }

    /**
     * @param input chords section
     * @param t  shift semitones count
     * @return chords section transposed by semitones
     */
    private fun transposeChords(input: String, t: Int): String {
        val out = StringBuilder()
        val chords = chordsSplitter.splitWithDelimiters(input)
        for (chord in chords) {
            val transposedChord = transposeChord(chord.str, t)
            out.append(transposedChord)
            out.append(chord.delimiter) // append the same delimiter which was splitting a chord
        }

        return out.toString()
    }

    /**
     * @param chord chord in format: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
     * @param t     shift semitones count
     * @return chord transposed by semitones
     */
    private fun transposeChord(chord: String, t: Int): String {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        val recognized = defaultChordsDetector.recognizeChord(chord) ?: return chord

        var chordNumber = recognized.first
        val suffix = recognized.second

        val family = getChordFamilyIndex(chordNumber) // minor or major
        // transpose by semitones
        chordNumber += t
        // restore the original chord family - wrapping
        while (getChordFamilyIndex(chordNumber) > family)
            chordNumber -= 12
        while (chordNumber < 0 || getChordFamilyIndex(chordNumber) < family)
            chordNumber += 12

        val chordName = chordsNotationSoundNames.get()[chordNumber]
        return chordName + suffix
    }

    private fun getChordFamilyIndex(chordNumber: Int): Int {
        return chordNumber / 12
    }

}
