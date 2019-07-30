package igrek.songbook.songpreview.lyrics

import com.google.common.base.Joiner
import java.util.*

class LyricsLine {

    var y: Int = 0

    val fragments = ArrayList<LyricsFragment>()

    fun addFragment(fragment: LyricsFragment) {
        fragments.add(fragment)
    }

    override fun toString(): String {
        return Joiner.on(" ").join(fragments)
    }
}
