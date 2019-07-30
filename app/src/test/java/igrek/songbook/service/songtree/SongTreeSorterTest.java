package igrek.songbook.service.songtree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import igrek.songbook.persistence.songsdb.Song;
import igrek.songbook.persistence.songsdb.SongCategory;
import igrek.songbook.persistence.songsdb.SongCategoryType;
import igrek.songbook.persistence.songsdb.SongStatus;
import igrek.songbook.songselection.tree.SongTreeItem;
import igrek.songbook.songselection.tree.SongTreeSorter;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SongTreeSorterTest {
	
	private SongTreeSorter sorter = new SongTreeSorter();
	
	@Test
	public void test_songTreeItemSorter() {
		// empty list
		assertSortedOrdering(Arrays.asList(), Arrays.asList());
		
		SongTreeItem song51 = songItem("51");
		SongTreeItem songCma = songItem("Ćma barowa");
		SongTreeItem songJestTaki = songItem("jest taki samotny dom");
		SongTreeItem songJolkaJolka = songItem("Jolka, jolka");
		SongTreeItem songZzz = songItem("Żśźęćół");
		SongTreeItem categoryCustom = categoryItem(SongCategoryType.CUSTOM, "Custom");
		SongTreeItem categoryBudka = categoryItem("Budka suflera");
		SongTreeItem categoryCc = categoryItem("Ćć");
		SongTreeItem categoryPinkFloyd = categoryItem("Pink Floyd");
		SongTreeItem categoryOthers = categoryItem(SongCategoryType.OTHERS, "Others");
		
		// one song
		assertSortedOrdering(Arrays.asList(song51), Arrays.asList(song51));
		
		// simple songs
		assertSortedOrdering(Arrays.asList(songJolkaJolka, songJestTaki), Arrays.asList(songJestTaki, songJolkaJolka));
		
		// categories
		assertSortedOrdering(Arrays.asList(categoryBudka, categoryOthers, categoryCustom, categoryPinkFloyd, categoryCc), Arrays
				.asList(categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers));
		
		// all
		assertSortedOrdering(Arrays.asList(categoryBudka, songZzz, songCma, songJolkaJolka, song51, categoryPinkFloyd, categoryOthers, categoryCustom, songJestTaki, categoryCc), Arrays
				.asList(song51, songCma, songJestTaki, songJolkaJolka, songZzz, categoryCustom, categoryBudka, categoryCc, categoryPinkFloyd, categoryOthers));
	}
	
	private SongTreeItem songItem(String title) {
		return songItem(title, SongCategoryType.OTHERS, "others", false);
	}
	
	private SongTreeItem songItem(String title, String artist) {
		return songItem(title, SongCategoryType.ARTIST, artist, false);
	}
	
	private SongTreeItem songItem(String title, SongCategoryType categoryType, String categoryName, boolean custom) {
		SongCategory category = new SongCategory(categoryType.getId(), categoryType, categoryName, false, categoryName, new ArrayList<>());
		Song song = new Song(1, title, category, null, 1, 0, 0, custom, title, null, null, false, null, null, SongStatus.PUBLISHED, null, null, null, null, null, null, null, null);
		return SongTreeItem.Companion.song(song);
	}
	
	private SongTreeItem categoryItem(String artist) {
		return categoryItem(SongCategoryType.ARTIST, artist);
	}
	
	private SongTreeItem categoryItem(SongCategoryType categoryType, String categoryName) {
		SongCategory category = new SongCategory(categoryType.getId(), categoryType, categoryName, false, categoryName, new ArrayList<>());
		return SongTreeItem.Companion.category(category);
	}
	
	private void assertSortedOrdering(List<SongTreeItem> in, List<SongTreeItem> outExpected) {
		List<SongTreeItem> out = sorter.sort(in);
		assertThat(out).isEqualTo(outExpected);
	}
	
}
