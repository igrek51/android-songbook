package igrek.songbook.chords.parser

import igrek.songbook.chords.model.LyricsFragment
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.model.LyricsTextType
import java.util.concurrent.atomic.AtomicBoolean

class LyricsExtractor(
    val trimWhitespaces: Boolean = true,
) {

    fun parseLyrics(content: String): LyricsModel {
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
        val brace = AtomicBoolean(false)
        val lines = rawLines.mapIndexed { index, rawLine ->
            val line = if (trimWhitespaces) rawLine.trim() else rawLine
            parseLine(line, bracket, brace, index)
        }.dropLastWhile { line -> line.isBlank }
        return LyricsModel(lines = lines)
    }

    private fun parseLine(
        rawLine: String,
        bracket: AtomicBoolean,
        brace: AtomicBoolean,
        lineIndex: Int,
    ): LyricsLine {
        val fragments = mutableListOf<LyricsFragment>()
        var frameStart = 0
        rawLine.forEachIndexed { index, character ->
            when (character) {
                '[' -> {
                    cutOffFragment(rawLine, fragments, bracket, brace, frameStart, index)
                    frameStart = index + 1
                    bracket.set(true)
                }
                ']' -> {
                    cutOffFragment(rawLine, fragments, bracket, brace, frameStart, index)
                    frameStart = index + 1
                    bracket.set(false)
                }
                '{' -> {
                    cutOffFragment(rawLine, fragments, bracket, brace, frameStart, index)
                    frameStart = index + 1
                    brace.set(true)
                }
                '}' -> {
                    cutOffFragment(rawLine, fragments, bracket, brace, frameStart, index)
                    frameStart = index + 1
                    brace.set(false)
                }
            }
        }
        cutOffFragment(rawLine, fragments, bracket, brace, frameStart, rawLine.length)

        return LyricsLine(fragments = fragments, primalIndex = lineIndex)
    }

    private fun cutOffFragment(
        rawLine: String,
        fragments: MutableList<LyricsFragment>,
        bracket: AtomicBoolean,
        brace: AtomicBoolean,
        frameStart: Int,
        nextIndex: Int,
    ) {
        if (nextIndex <= frameStart)
            return

        val fragment = rawLine.substring(frameStart, nextIndex)
        val type = when {
            brace.get() -> LyricsTextType.COMMENT
            bracket.get() -> LyricsTextType.CHORDS
            else -> LyricsTextType.REGULAR_TEXT
        }
        fragments.add(LyricsFragment(text = fragment, type = type))
    }

}
