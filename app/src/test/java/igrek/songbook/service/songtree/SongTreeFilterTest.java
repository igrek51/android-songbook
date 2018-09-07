package igrek.songbook.service.songtree;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SongTreeFilterTest {
	
	@Test
	public void test_matchesNameFilter() {
		
		SongTreeItem songItem = mock(SongTreeItem.class, Mockito.RETURNS_DEEP_STUBS);
		
		when(songItem.getSong().getCategory().getDisplayName()).thenReturn("Budka suflera");
		when(songItem.getSong().getTitle()).thenReturn("Jolka jolka ążśźęćół ĄĄŻŚŹĘĆ Żółć Łódź");
		// test mockito
		assertThat(songItem.getSong().getCategory().getDisplayName()).isEqualTo("Budka suflera");
		
		assertThat(new SongTreeFilter("Budka").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("budka").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("uFL udK").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("jolka suflera").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("dupka").matchesNameFilter(songItem)).isFalse();
		assertThat(new SongTreeFilter("dupka suflera").matchesNameFilter(songItem)).isFalse();
		// polish letters
		assertThat(new SongTreeFilter("żółć łÓDŹ").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("zolc").matchesNameFilter(songItem)).isTrue();
		assertThat(new SongTreeFilter("azszecol aazszec lodz zolc").matchesNameFilter(songItem)).isTrue();
		
	}
}
