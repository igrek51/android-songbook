package igrek.songbook.service.songtree

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import igrek.songbook.persistence.model.CategoryType
import igrek.songbook.songselection.search.SongTreeFilter
import igrek.songbook.songselection.tree.SongTreeItem
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class SongTreeFilterTest {

    @Test
    fun test_matchesNameFilter() {

        val songItem = mock(SongTreeItem::class.java, Mockito.RETURNS_DEEP_STUBS)

        `when`(songItem.song!!.category.type).thenReturn(CategoryType.ARTIST)
        `when`(songItem.song!!.category.name).thenReturn("Budka suflera")
        `when`(songItem.song!!.title).thenReturn("Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź")
        // test mockito
        assertThat(songItem.song!!.category.name).isEqualTo("Budka suflera")

        assertThat(songItem.song!!
                .displayName()).isEqualTo("Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź - Budka suflera")

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

        val songItem = mock(SongTreeItem::class.java, Mockito.RETURNS_DEEP_STUBS)

        `when`(songItem.song!!.category.type).thenReturn(CategoryType.ARTIST)
        `when`(songItem.song!!.category.name).thenReturn("Budka suflera")
        `when`(songItem.song!!.title).thenReturn("he's dupa")

        assertThat(SongTreeFilter("d'upa hes").songMatchesNameFilter(songItem)).isTrue()
    }
}
