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

    fun detectChordsUnaligned(line: String): List<ChordSegment> {
        val segments = mutableListOf<ChordSegment>()
        var offset = 0
        line.replace(Regex("""\[(.*?)]""")) { matchResult ->
            val start = matchResult.range.first - offset
            val chord = matchResult.groupValues[1]
            segments.add(ChordSegment(chord, start))
            offset += 2
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
        val maxStart = chords.map { it.start }.maxOrNull() ?: 0
        var result = line.padEnd(maxStart, ' ')
        chords.reversed().forEach { chord ->
            val start = chord.start
            val splitPoint = splitPoint(start, line)
            val before = result.take(splitPoint)
            val after = result.drop(splitPoint)
            result = before + "[${chord.chord}]" + after
        }
        return result
    }

    private fun splitPoint(draft: Int, line: String): Int {
        val before = line.take(draft)
        val after = line.drop(draft)

        // solve "w[a]ord" unalignment
        val minus2 = before.takeLast(2).dropLast(1)
        val minus1 = before.takeLast(1)
        val plus1 = after.take(1)
        if (
            (minus2.isEmpty() || !minus2.hasLetters()) &&
            minus1.isNotEmpty() && minus1.hasLetters() &&
            plus1.isNotEmpty() && plus1.hasLetters()
        ) {
            return draft - 1
        }

        return draft
    }

    private fun String.hasLetters(): Boolean {
        return this.matches(Regex("""^\p{L}+$"""))
    }
}