package igrek.songbook.service.database;

import java.util.List;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.song.Song;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class PersistenceService {
	
	@Inject
	SqlQueryService sqlQueryService;
	
	@Inject
	SongsDatabaseService songsDatabaseService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public PersistenceService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public List<Song> readAllSongs() {
		return sqlQueryService.readAllSongs();
	}
	
}
