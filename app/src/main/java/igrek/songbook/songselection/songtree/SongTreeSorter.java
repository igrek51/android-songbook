package igrek.songbook.songselection.songtree;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import igrek.songbook.model.songsdb.SongCategoryType;
import igrek.songbook.songselection.SongTreeItem;

public class SongTreeSorter {
	
	private final Locale locale = new Locale("pl", "PL");
	private Collator stringCollator = Collator.getInstance(locale);
	
	private Function<SongTreeItem, SongCategoryType> categoryTypeExtractor = (item) -> {
		if (item == null || item.getCategory() == null)
			return null;
		return item.getCategory().getType();
	};
	
	private Ordering<SongTreeItem> categorySongOrdering = Ordering.natural()
			.onResultOf(SongTreeItem::isCategory);
	
	private Ordering<SongTreeItem> categoryTypeOrdering = //
			Ordering.explicit(SongCategoryType.CUSTOM, SongCategoryType.ARTIST, SongCategoryType.OTHERS)
					.nullsLast()
					.onResultOf(categoryTypeExtractor);
	
	private Ordering<SongTreeItem> itemNameOrdering = Ordering.from((lhs, rhs) -> {
		String lName = lhs.getSimpleName().toLowerCase(locale);
		String rName = rhs.getSimpleName().toLowerCase(locale);
		return stringCollator.compare(lName, rName);
	});
	
	private Ordering<SongTreeItem> songTreeItemOrdering = categorySongOrdering // songs before categories
			.compound(categoryTypeOrdering) // CUSTOM, ARTIST (...), OTHERS
			.compound(itemNameOrdering); // sort by name
	
	
	public List<SongTreeItem> sort(List<SongTreeItem> items) {
		// make it modifiable
		List<SongTreeItem> modifiableList = new ArrayList<>(items);
		Collections.sort(modifiableList, songTreeItemOrdering);
		return modifiableList;
	}
}
