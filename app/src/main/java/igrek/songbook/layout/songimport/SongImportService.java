package igrek.songbook.layout.songimport;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.persistence.SongsRepository;

public class SongImportService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	SongsRepository songsRepository;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongImportService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void importSong(String filename, String content) {
		
		logger.debug("filename: " + filename);
		
		uiInfoService.showInfo(R.string.new_song_has_been_imported);
	}
}
