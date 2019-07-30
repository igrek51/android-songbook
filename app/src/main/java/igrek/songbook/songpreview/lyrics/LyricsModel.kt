package igrek.songbook.songpreview.lyrics

import com.google.common.base.Joiner
import java.util.*

class LyricsModel {

    val lines: MutableList<LyricsLine>

    init {
        lines = ArrayList()
    }

    fun addLines(lines: List<LyricsLine>) {
        this.lines.addAll(lines)
    }

    override fun toString(): String {
        return Joiner.on("\n").join(lines)
    }
}
