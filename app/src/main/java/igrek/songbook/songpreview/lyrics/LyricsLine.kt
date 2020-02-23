package igrek.songbook.songpreview.lyrics

import com.google.common.base.Joiner

data class LyricsLine(
        var y: Int = 0,
        var fragments: MutableList<LyricsFragment> = mutableListOf()
) {

    fun addFragment(fragment: LyricsFragment) {
        fragments.add(fragment)
    }

    override fun toString(): String {
        return Joiner.on(" ").join(fragments)
    }

    fun isBlank(): Boolean {
        return fragments.all { fragment -> fragment.text.isBlank() }
    }
}
