package igrek.songbook.service.songtree

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.songselection.listview.items.SongTreeItem
import igrek.songbook.songselection.tree.SongTreeSorter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
        val categoryCustom = categoryItem(CategoryType.CUSTOM, "Custom")
        val categoryBudka = categoryItem("Budka suflera")
        val categoryCc = categoryItem("Ćć")
        val categoryPinkFloyd = categoryItem("Pink Floyd")
        val categoryOthers = categoryItem(CategoryType.OTHERS, "Others")

        // one song
        assertSortedOrdering(listOf(song51), listOf(song51))

        // simple songs
        assertSortedOrdering(listOf(songJolkaJolka, songJestTaki), listOf(songJestTaki, songJolkaJolka))

        // categories
        assertSortedOrdering(listOf(categoryBudka, categoryOthers, categoryCustom, categoryPinkFloyd, categoryCc), listOf(categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers))

        // all
        assertSortedOrdering(listOf(categoryBudka, songZzz, songCma, songJolkaJolka, song51, categoryPinkFloyd, categoryOthers, categoryCustom, songJestTaki, categoryCc), listOf(song51, songCma, songJestTaki, songJolkaJolka, songZzz, categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers))
    }

    private fun songItem(title: String, categoryType: CategoryType = CategoryType.OTHERS, categoryName: String = "others", custom: Boolean = false): SongTreeItem {
        val category = Category(categoryType.id, categoryType, categoryName, false)
        val song = Song(
                id = "1",
                title = title,
                categories = mutableListOf(category),
                content = null,
                versionNumber = 1,
                createTime = 0,
                updateTime = 0,
                comment = null,
                preferredKey = null,
                locked = false,
                lockPassword = null,
                author = null,
                status = SongStatus.PUBLISHED
        )
        return SongTreeItem.song(song)
    }

    private fun categoryItem(artist: String): SongTreeItem {
        return categoryItem(CategoryType.ARTIST, artist)
    }

    private fun categoryItem(categoryType: CategoryType, categoryName: String): SongTreeItem {
        val category = Category(categoryType.id, categoryType, categoryName, false)
        return SongTreeItem.category(category)
    }

    private fun assertSortedOrdering(`in`: List<SongTreeItem>, outExpected: List<SongTreeItem>) {
        val out = sorter.sort(`in`)
        assertThat(out).isEqualTo(outExpected)
    }

}
