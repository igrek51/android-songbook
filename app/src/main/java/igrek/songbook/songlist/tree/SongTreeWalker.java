package igrek.songbook.songlist.tree;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.exception.NoParentItemException;
import igrek.songbook.domain.songsdb.SongCategory;

public class SongTreeWalker {
	
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
	}
	
	public void goToCategory(SongCategory category) {
		currentCategory = category;
	}
	
	public SongCategory getCurrentCategory() {
		return currentCategory;
	}
	
	public boolean isCategorySelected() {
		return currentCategory != null;
	}
}
