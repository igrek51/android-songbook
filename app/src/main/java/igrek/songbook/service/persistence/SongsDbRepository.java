package igrek.songbook.service.persistence;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.song.Song;
import igrek.songbook.domain.song.SongCategory;
import igrek.songbook.domain.song.SongCategoryType;
import igrek.songbook.domain.song.SongsDb;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UiResourceService;

public class SongsDbRepository {
	
	@Inject
	PersistenceService persistenceService;
	@Inject
	UiResourceService uiResourceService;
	
	private SongsDb songsDb;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongsDbRepository() {
		DaggerIoc.getFactoryComponent().inject(this);
		initSongsDb();
	}
	
	private void initSongsDb() {
		List<Song> songs = persistenceService.readAllSongs();
		Multimap<String, Song> categorySongs = ArrayListMultimap.create();
		
		for (Song song : songs) {
			String categoryName = song.getCategoryName();
			categorySongs.put(categoryName, song);
		}
		
		// group by categories
		List<SongCategory> categories = new ArrayList<>();
		for (String categoryName : categorySongs.keySet()) {
			List<Song> songsOfCategory = new ArrayList<>(categorySongs.get(categoryName));
			SongCategory category;
			if (categoryName != null) {
				category = new SongCategory(categoryName, SongCategoryType.ARTIST, songsOfCategory);
			} else {
				categoryName = uiResourceService.resString(SongCategoryType.OTHERS.getLocaleStringId());
				category = new SongCategory(categoryName, SongCategoryType.OTHERS, songsOfCategory);
			}
			categories.add(category);
		}
		
		long versionNumber = 1;
		songsDb = new SongsDb(versionNumber, categories, songs);
	}
	
	public SongsDb getSongsDb() {
		return songsDb;
	}
}
