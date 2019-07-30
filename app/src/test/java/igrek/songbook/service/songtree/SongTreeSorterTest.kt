package igrek.songbook.service.songtree

import assertk.assertThat
import assertk.assertions.isEqualTo
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategory
import igrek.songbook.persistence.songsdb.SongCategoryType
import igrek.songbook.persistence.songsdb.SongStatus
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeSorter
import org.junit.Test
import java.util.*

class SongTreeSorterTest {

    private val sorter = SongTreeSorter()

    @Test
    fun test_songTreeItemSorter() {
        // empty list
        assertSortedOrdering(listOf(), listOf())

        val song51 = songItem("51")
        val songCma = songItem("Ćma barowa")
        val songJestTaki = songItem("jest taki samotny dom")
        val songJolkaJolka = songItem("Jolka, jolka")
        val songZzz = songItem("Żśźęćół")
        val categoryCustom = categoryItem(SongCategoryType.CUSTOM, "Custom")
        val categoryBudka = categoryItem("Budka suflera")
        val categoryCc = categoryItem("Ćć")
        val categoryPinkFloyd = categoryItem("Pink Floyd")
        val categoryOthers = categoryItem(SongCategoryType.OTHERS, "Others")

        // one song
        assertSortedOrdering(listOf(song51), listOf(song51))

        // simple songs
        assertSortedOrdering(listOf(songJolkaJolka, songJestTaki), listOf(songJestTaki, songJolkaJolka))

        // categories
        assertSortedOrdering(listOf(categoryBudka, categoryOthers, categoryCustom, categoryPinkFloyd, categoryCc), listOf(categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers))

        // all
        assertSortedOrdering(listOf(categoryBudka, songZzz, songCma, songJolkaJolka, song51, categoryPinkFloyd, categoryOthers, categoryCustom, songJestTaki, categoryCc), listOf(song51, songCma, songJestTaki, songJolkaJolka, songZzz, categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers))
    }

    private fun songItem(title: String, categoryType: SongCategoryType = SongCategoryType.OTHERS, categoryName: String = "others", custom: Boolean = false): SongTreeItem {
        val category = SongCategory(categoryType.id, categoryType, categoryName, false, categoryName, ArrayList())
        val song = Song(1, title, category, null, 1, 0, 0, custom, title, null, null, false, null, null, SongStatus.PUBLISHED, null, null, null, null, null, null, null, null)
        return SongTreeItem.song(song)
    }

    private fun categoryItem(artist: String): SongTreeItem {
        return categoryItem(SongCategoryType.ARTIST, artist)
    }

    private fun categoryItem(categoryType: SongCategoryType, categoryName: String): SongTreeItem {
        val category = SongCategory(categoryType.id, categoryType, categoryName, false, categoryName, ArrayList())
        return SongTreeItem.category(category)
    }

    private fun assertSortedOrdering(`in`: List<SongTreeItem>, outExpected: List<SongTreeItem>) {
        val out = sorter.sort(`in`)
        assertThat(out).isEqualTo(outExpected)
    }

}
