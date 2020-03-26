package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import java.util.concurrent.atomic.AtomicBoolean

class LyricsParser {

    fun parseContent(content: String): LyricsModel {
        val normalized = content.replace("\t", " ").trim()
        val rawLines = normalized.lines().dropLastWhile { it.isEmpty() }

        return parseLines(rawLines)
    }

    private fun parseLines(rawLines: List<String>): LyricsModel {
        val bracket = AtomicBoolean(false)
        val lines = rawLines.map { rawLine ->
            parseLine(rawLine.trim(), bracket)
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
