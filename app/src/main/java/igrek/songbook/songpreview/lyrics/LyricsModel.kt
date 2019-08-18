package igrek.songbook.songpreview.lyrics

import com.google.common.base.Joiner

data class LyricsModel(
        val lines: MutableList<LyricsLine> = mutableListOf()
) {

    fun addLines(lines: List<LyricsLine>) {
        this.lines.addAll(lines)
    }

    override fun toString(): String {
        return Joiner.on("\n").join(lines)
    }
}
