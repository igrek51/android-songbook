package igrek.songbook.service.songtree;

import java.util.List;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.exception.NoParentItemException;
import igrek.songbook.domain.songsdb.SongCategory;

public class SongTreeWalker {
	
	private List<SongTreeItem> currentItems;
	private SongCategory currentCategory;
	
	public SongTreeWalker() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void goUp() throws NoParentItemException {
		if (currentCategory == null)
			throw new NoParentItemException();
		goToAllCategories();
	}
	
	public void goToAllCategories() {
		currentCategory = null;
		currentItems = null;
	}
	
	public void goToCategory(SongCategory category) {
		currentCategory = category;
		currentItems = null;
	}
	
	public void goToAllSongs() {
		currentCategory = null;
		currentItems = null;
	}
	
	public void setCurrentItems(List<SongTreeItem> items) {
		this.currentItems = items;
	}
	
	public List<SongTreeItem> getCurrentItems() {
		return currentItems;
	}
	
	public SongCategory getCurrentCategory() {
		return currentCategory;
	}
	
	public boolean isCategorySelected() {
		return currentCategory != null;
	}
}
