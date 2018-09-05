package igrek.songbook.service.persistence;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.domain.songsdb.SongCategory;
import igrek.songbook.domain.songsdb.SongsDb;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.persistence.database.LocalDatabaseService;
import igrek.songbook.service.persistence.database.SqlQueryService;

public class SongsDbRepository {
	
	@Inject
	SqlQueryService sqlQueryService;
	@Inject
	LocalDatabaseService localDatabaseService;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	UiInfoService uiInfoService;
	
	private SongsDb songsDb;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongsDbRepository() {
		DaggerIoc.getFactoryComponent().inject(this);
		localDatabaseService.checkDatabaseValid();
		initSongsDb();
	}
	
	public void updateDb() {
		localDatabaseService.recreateDb();
		initSongsDb();
		uiInfoService.showInfo(R.string.ui_db_is_uptodate);
	}
	
	private void initSongsDb() {
		long versionNumber = sqlQueryService.readDbVersionNumber();
		
		List<SongCategory> categories = sqlQueryService.readAllCategories();
		List<Song> songs = sqlQueryService.readAllSongs(categories);
		
		// group by categories
		Multimap<SongCategory, Song> categorySongs = ArrayListMultimap.create();
		for (Song song : songs) {
			categorySongs.put(song.getCategory(), song);
		}
		
		for (SongCategory category : categories) {
			Collection<Song> songsOfCategory = categorySongs.get(category);
			category.setSongs(new ArrayList<>(songsOfCategory));
			// refill category display name
			if (category.getName() != null) {
				category.setDisplayName(category.getName());
			} else {
				String displayName = uiResourceService.resString(category.getType()
						.getLocaleStringId());
				category.setDisplayName(displayName);
			}
		}
		
		songsDb = new SongsDb(versionNumber, categories, songs);
	}
	
	public SongsDb getSongsDb() {
		return songsDb;
	}
}
