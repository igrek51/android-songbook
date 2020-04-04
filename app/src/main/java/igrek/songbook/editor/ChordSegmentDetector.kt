package igrek.songbook.editor

class ChordSegmentDetector {
    fun detectChords(line: String): List<ChordSegment> {
        val segments = mutableListOf<ChordSegment>()
        line.replace(Regex("""\[(.*?)]""")) { matchResult ->
            val start = matchResult.range.first
            val chord = matchResult.groupValues[1]
            segments.add(ChordSegment(chord, start))
            "[$chord]"
        }
        return segments
    }
}

data class ChordSegment(
        val chord: String,
        val start: Int,
)

class ChordSegmentApplier {
    fun applyChords(line: String, chords: List<ChordSegment>): String {
        val maxStart = chords.map { it.start }.max() ?: 0
        var result = line.padEnd(maxStart, ' ')
        chords.reversed().forEach { chord ->
            val start = chord.start
            result = result.take(start) + "[${chord.chord}]" + result.drop(start)
        }
        return result
    }
}