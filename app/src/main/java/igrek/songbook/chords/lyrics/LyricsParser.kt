package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import java.util.concurrent.atomic.AtomicBoolean

class LyricsParser(
        val trimWhitespaces: Boolean = true,
) {

    fun parseContent(content: String): LyricsModel {
        val normalized = if (trimWhitespaces) normalizeContent(content) else content
        val rawLines = normalized.lines().dropLastWhile { it.isEmpty() }
        return parseLines(rawLines)
    }

    private fun normalizeContent(content: String): String {
        return content
                .replace("\t", " ")
                .replace("\u00A0", " ") // no-break space
                .trim()
    }

    private fun parseLines(rawLines: List<String>): LyricsModel {
        val bracket = AtomicBoolean(false)
        val lines = rawLines.map { rawLine ->
            val line = if (trimWhitespaces) rawLine.trim() else rawLine
            parseLine(line, bracket)
        }.dropLastWhile { line -> line.isBlank() }
        return LyricsModel(lines = lines)
    }

    private fun parseLine(rawLine: String, bracket: AtomicBoolean): LyricsLine {
        val fragments = mutableListOf<LyricsFragment>()
        var frameStart = 0
        rawLine.forEachIndexed { index, character ->
            when (character) {
                '[' -> {
                    cutOffFragment(rawLine, fragments, bracket, frameStart, index)
                    frameStart = index + 1
                    bracket.set(true)
                }
                ']' -> {
                    cutOffFragment(rawLine, fragments, bracket, frameStart, index)
                    frameStart = index + 1
                    bracket.set(false)
                }
            }
        }
        cutOffFragment(rawLine, fragments, bracket, frameStart, rawLine.length)

        return LyricsLine(fragments = fragments)
    }

    private fun cutOffFragment(rawLine: String, fragments: MutableList<LyricsFragment>, bracket: AtomicBoolean, frameStart: Int, nextIndex: Int) {
        if (nextIndex <= frameStart)
            return

        val fragment = rawLine.substring(frameStart, nextIndex)
        val type = when (bracket.get()) {
            true -> LyricsTextType.CHORDS
            false -> LyricsTextType.REGULAR_TEXT
        }
        fragments.add(LyricsFragment(text = fragment, type = type))
    }

}
