package igrek.songbook.service.songtree;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.exception.NoParentItemException;
import igrek.songbook.domain.song.Song;
import igrek.songbook.domain.song.SongCategory;
import igrek.songbook.domain.song.SongsDb;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class SongTreeWalker {
	
	private Logger logger = LoggerFactory.getLogger();
	
	private List<SongTreeItem> currentItems;
	private SongCategory currentCategory;
	private String itemFilter;
	
	private State state = State.CategoriesList;
	
	private Collator stringCollator = Collator.getInstance(new Locale("pl", "PL"));
	
	private enum State {
		CategoriesList, CategorySongs, AllSongs
	}
	
	public SongTreeWalker() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void goUp() throws NoParentItemException {
		if (state == State.CategoriesList || state == State.AllSongs)
			throw new NoParentItemException();
		goToAllCategories();
	}
	
	public void goToAllCategories() {
		state = State.CategoriesList;
		currentCategory = null;
		currentItems = null;
	}
	
	public void goToCategory(SongCategory category) {
		state = State.CategorySongs;
		currentCategory = category;
		currentItems = null;
	}
	
	public void goToAllSongs() {
		state = State.AllSongs;
		currentCategory = null;
		currentItems = null;
		itemFilter = null;
	}
	
	public void updateItems(SongsDb songsDb) {
		currentItems = new ArrayList<>();
		
		if (state == State.CategoriesList) {
			for (SongCategory category : songsDb.getCategories()) {
				SongTreeItem item = SongTreeItem.category(category);
				currentItems.add(item);
			}
		} else if (state == State.CategorySongs) {
			for (Song song : currentCategory.getSongs()) {
				SongTreeItem item = SongTreeItem.song(song);
				currentItems.add(item);
			}
		} else if (state == State.AllSongs) {
			for (Song song : songsDb.getAllSongs()) {
				SongTreeItem item = SongTreeItem.song(song);
				// filtering
				if (itemFilter == null) {
					currentItems.add(item);
				} else {
					// must contain every part
					String fullName = song.getCategoryName() + " - " + song.getTitle();
					if (containsEveryPart(fullName, itemFilter)) {
						currentItems.add(item);
					}
				}
			}
		}
		
		Collections.sort(currentItems, (lhs, rhs) -> {
			// categories first
			if (lhs.isCategory() && rhs.isSong())
				return -1;
			if (lhs.isSong() && rhs.isCategory())
				return +1;
			return stringCollator.compare(lhs.getSimpleName().toLowerCase(), rhs.getSimpleName()
					.toLowerCase());
		});
	}
	
	private boolean containsEveryPart(String input, String partsFilter) {
		String[] parts = partsFilter.split(" ");
		for (String part : parts) {
			if (!input.contains(part))
				return false;
		}
		return true;
	}
	
	public List<SongTreeItem> getCurrentItems() {
		return currentItems;
	}
	
	public SongCategory getCurrentCategory() {
		return currentCategory;
	}
	
	public boolean isCategorySelected() {
		return state == State.CategorySongs;
	}
	
	public void setItemFilter(String itemFilter) {
		this.itemFilter = itemFilter;
	}
}
