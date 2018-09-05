package igrek.songbook.service.persistence;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.domain.songsdb.SongCategory;
import igrek.songbook.domain.songsdb.SongsDb;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
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
	
	private SongsDb songsDb;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongsDbRepository() {
		DaggerIoc.getFactoryComponent().inject(this);
		initSongsDb();
	}
	
	public void updateDb() {
		localDatabaseService.recreateDb();
		initSongsDb();
	}
	
	private void initSongsDb() {
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
		}
		
		long versionNumber = sqlQueryService.readDbVersionNumber();
		songsDb = new SongsDb(versionNumber, categories, songs);
	}
	
	public SongsDb getSongsDb() {
		return songsDb;
	}
}
