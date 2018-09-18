package igrek.songbook.layout.songpreview;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

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
		String comment = or(song.getComment(), "None");
		String preferredKey = or(song.getPreferredKey(), "None");
		String message = uiResourceService.resString(R.string.song_details, song.getTitle(), song.getCategory()
				.getDisplayName(), comment, preferredKey, song.getVersionNumber());
		String title = uiResourceService.resString(R.string.song_details_title);
		uiInfoService.showDialog(title, message);
	}
	
	private String or(String s, String defaultValue) {
		return s == null ? defaultValue : s;
	}
	
}
