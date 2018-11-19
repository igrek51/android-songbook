package igrek.songbook.songedit;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.model.songsdb.Song;
import igrek.songbook.model.songsdb.SongCategory;
import igrek.songbook.model.songsdb.SongCategoryType;
import igrek.songbook.model.songsdb.SongStatus;
import igrek.songbook.persistence.SongsRepository;

public class SongEditService {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	SongsRepository songsRepository;
	@Inject
	LayoutController layoutController;
	@Inject
	Lazy<EditSongLayoutController> importSongLayoutController;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongEditService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showAddSongScreen() {
		importSongLayoutController.get().setCurrentSong(null);
		layoutController.showImportSong();
	}
	
	public void showEditSongScreen(Song song) {
		importSongLayoutController.get().setCurrentSong(song);
		layoutController.showImportSong();
	}
	
	public Song addCustomSong(String title, String customCategoryName, String content) {
		long versionNumber = 1;
		long now = new Date().getTime();
		SongCategory category = songsRepository.getCustomCategoryByTypeId(SongCategoryType.CUSTOM.getId());
		Song newSong = new Song(0, title, category, content, versionNumber, now, now, true, title, null, null, false, null, null, SongStatus.PROPOSED, customCategoryName, null);
		songsRepository.saveImportedSong(newSong);
		return newSong;
	}
	
	public void updateSong(Song currentSong, String songTitle, String customCategoryName, String songContent) {
		currentSong.setTitle(songTitle);
		currentSong.setContent(songContent);
		currentSong.setCustomCategoryName(customCategoryName);
		songsRepository.updateCustomSong(currentSong);
	}
	
	public void removeSong(Song currentSong) {
		songsRepository.removeCustomSong(currentSong);
	}
}
