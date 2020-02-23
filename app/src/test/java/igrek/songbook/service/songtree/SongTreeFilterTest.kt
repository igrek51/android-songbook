package igrek.songbook.service.songtree

import org.assertj.core.api.Assertions.assertThat
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.songselection.search.SongTreeFilter
import igrek.songbook.songselection.tree.SongTreeItem
import org.junit.Test

class SongTreeFilterTest {

    @Test
    fun test_matchesNameFilter() {
        val songItem = SongTreeItem.song(Song(
                id = 1,
                title = "Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź",
                categories = mutableListOf(
                        Category(1, type = CategoryType.ARTIST, name = "Budka suflera")
                ),
                status = SongStatus.PUBLISHED
        ))

        assertThat(songItem.song!!.categories[0].name).isEqualTo("Budka suflera")

        assertThat(songItem.song!!.displayName())
                .isEqualTo("Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź - Budka suflera")

        assertThat(SongTreeFilter("Budka").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("budka").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("uFL udK").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("jolka suflera").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("dupka").songMatchesNameFilter(songItem)).isFalse()
        assertThat(SongTreeFilter("dupka suflera").songMatchesNameFilter(songItem)).isFalse()
        // polish letters
        assertThat(SongTreeFilter("żółć łÓDŹ").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("zolc").songMatchesNameFilter(songItem)).isTrue()
        assertThat(SongTreeFilter("azszecol aazszec lodz zolc").songMatchesNameFilter(songItem))
                .isTrue()
    }

    @Test
    fun test_filteringWithQuotes() {
        val songItem = SongTreeItem.song(Song(
                id = 1,
                title = "he's dupa",
                categories = mutableListOf(
                        Category(1, type = CategoryType.ARTIST, name = "Budka suflera")
                ),
                status = SongStatus.PUBLISHED
        ))

        assertThat(SongTreeFilter("d'upa hes").songMatchesNameFilter(songItem)).isTrue()
    }
}
