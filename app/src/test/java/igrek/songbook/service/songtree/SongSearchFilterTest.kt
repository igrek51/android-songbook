package igrek.songbook.service.songtree

import org.assertj.core.api.Assertions.assertThat
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongStatus
import igrek.songbook.songselection.listview.items.SongListItem
import igrek.songbook.songselection.search.SongSearchFilter
import org.junit.Test

class SongSearchFilterTest {

    @Test
    fun test_matchesNameFilter() {
        val songItem = SongListItem(Song(
                id = "1",
                title = "Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź",
                categories = mutableListOf(
                        Category(1, type = CategoryType.ARTIST, name = "Budka suflera")
                ),
                status = SongStatus.PUBLISHED
        ))

        assertThat(songItem.song.categories[0].name).isEqualTo("Budka suflera")

        assertThat(songItem.song.displayName())
                .isEqualTo("Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź - Budka suflera")

        assertThat(SongSearchFilter("Budka").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("budka").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("uFL udK").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("jolka suflera").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("dupka").matchSong(songItem.song)).isFalse()
        assertThat(SongSearchFilter("dupka suflera").matchSong(songItem.song)).isFalse()
        // polish letters
        assertThat(SongSearchFilter("żółć łÓDŹ").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("zolc").matchSong(songItem.song)).isTrue()
        assertThat(SongSearchFilter("azszecol aazszec lodz zolc").matchSong(songItem.song))
                .isTrue()
    }

    @Test
    fun test_filteringWithQuotes() {
        val songItem = SongListItem(Song(
                id = "1",
                title = "he's dupa",
                categories = mutableListOf(
                        Category(1, type = CategoryType.ARTIST, name = "Budka suflera")
                ),
                status = SongStatus.PUBLISHED
        ))

        assertThat(SongSearchFilter("d'upa hes").matchSong(songItem.song)).isTrue()
    }
}
