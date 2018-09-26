package igrek.songbook.layout.songimport;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.domain.songsdb.SongCategory;
import igrek.songbook.domain.songsdb.SongCategoryType;
import igrek.songbook.domain.songsdb.SongStatus;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.persistence.SongsRepository;

public class SongImportService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	SongsRepository songsRepository;
	@Inject
	LayoutController layoutController;
	@Inject
	Lazy<ImportSongLayoutController> importSongLayoutController;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongImportService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void importSong(String title, String content) {
		
		long versionNumber = 1;
		long now = new Date().getTime();
		SongCategory category = songsRepository.getCustomCategoryByTypeId(SongCategoryType.CUSTOM.getId());
		Song newSong = new Song(0, title, category, content, versionNumber, now, now, true, title, null, null, false, null, null, SongStatus.PROPOSED);
		
		songsRepository.saveImportedSong(newSong);
		
		uiInfoService.showInfo(R.string.new_song_has_been_imported);
	}
	
	public void showImportSongScreen(String filename, String content) {
		importSongLayoutController.get().setImportedSong(filename, content);
		layoutController.showImportSong();
	}
}
