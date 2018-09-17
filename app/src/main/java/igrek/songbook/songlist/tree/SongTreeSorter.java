package igrek.songbook.songlist.tree;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import igrek.songbook.domain.songsdb.SongCategoryType;

public class SongTreeSorter {
	
	private final Locale locale = new Locale("pl", "PL");
	private Collator stringCollator = Collator.getInstance(locale);
	
	private Comparator<SongTreeItem> songTreeItemComparator = (lhs, rhs) -> {
		// categories first
		if (lhs.isCategory() && rhs.isSong())
			return -1;
		if (lhs.isSong() && rhs.isCategory())
			return +1;
		// special categories at the end
		if (lhs.isCategory() && rhs.isCategory()) {
			if (lhs.getCategory().getType() != SongCategoryType.ARTIST || rhs.getCategory()
					.getType() != SongCategoryType.ARTIST) {
				return Long.compare(lhs.getCategory().getType().getId(), rhs.getCategory()
						.getType()
						.getId());
			}
		}
		// string comparison with localisation support
		String lName = lhs.getSimpleName().toLowerCase(locale);
		String rName = rhs.getSimpleName().toLowerCase(locale);
		return stringCollator.compare(lName, rName);
	};
	
	public SongTreeSorter() {
	}
	
	public void sort(List<SongTreeItem> items) {
		Collections.sort(items, songTreeItemComparator);
	}
}
