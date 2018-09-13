package igrek.songbook.service.layout.songpreview;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;

public class SongDetailsService {
	
	@Inject
	AppCompatActivity activity;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongDetailsService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showSongDetails(Song song) {
		String comment = orEmpty(song.getComment());
		String preferredKey = orEmpty(song.getPreferredKey());
		String message = uiResourceService.resString(R.string.song_details, song.getTitle(), song.getCategory()
				.getDisplayName(), comment, preferredKey, song.getVersionNumber());
		String title = uiResourceService.resString(R.string.song_details_title);
		uiInfoService.showDialog(title, message);
	}
	
	private String orEmpty(String s) {
		return s == null ? "" : s;
	}
	
}
