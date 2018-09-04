package igrek.songbook.service.persistence;

import java.util.List;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.song.Song;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.persistence.database.LocalDatabaseService;
import igrek.songbook.service.persistence.database.SqlQueryService;

public class PersistenceService {
	
	@Inject
	SqlQueryService sqlQueryService;
	
	@Inject
	LocalDatabaseService localDatabaseService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public PersistenceService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public List<Song> readAllSongs() {
		return sqlQueryService.readAllSongs();
	}
	
}
