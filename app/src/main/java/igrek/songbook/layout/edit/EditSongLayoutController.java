package igrek.songbook.layout.edit;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.errorcheck.SafeClickListener;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.LayoutState;
import igrek.songbook.layout.MainLayout;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.system.SoftKeyboardService;

public class EditSongLayoutController implements MainLayout {
	
	@Inject
	LayoutController layoutController;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	AppCompatActivity activity;
	@Inject
	NavigationMenuController navigationMenuController;
	@Inject
	Lazy<SongEditService> songImportService;
	@Inject
	SoftKeyboardService softKeyboardService;
	@Inject
	Lazy<SongImportFileChooser> songImportFileChooser;
	
	private Song currentSong;
	private String songTitle;
	private String songContent;
	
	private Logger logger = LoggerFactory.getLogger();
	private EditText songTitleEdit;
	private EditText songContentEdit;
	
	public EditSongLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void showLayout(View layout) {
		// Toolbar
		Toolbar toolbar1 = layout.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		ActionBar actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		// navigation menu button
		ImageButton navMenuButton = layout.findViewById(R.id.navMenuButton);
		navMenuButton.setOnClickListener((v) -> navigationMenuController.navDrawerShow());
		
		songTitleEdit = layout.findViewById(R.id.songTitleEdit);
		songContentEdit = layout.findViewById(R.id.songContentEdit);
		Button saveSongButton = layout.findViewById(R.id.saveSongButton);
		saveSongButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				saveSong();
			}
		});
		
		Button removeSongButton = layout.findViewById(R.id.removeSongButton);
		removeSongButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				removeSong();
			}
		});
		
		Button importFromFileButotn = layout.findViewById(R.id.importFromFileButotn);
		importFromFileButotn.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				importContentFromFile();
			}
		});
		
		Button addChordButton = layout.findViewById(R.id.addChordButton);
		addChordButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				onAddChordClick();
			}
		});
		
		songTitleEdit.setText(songTitle);
		songContentEdit.setText(songContent);
	}
	
	private void onAddChordClick() {
		String edited = songContentEdit.getText().toString();
		int selStart = songContentEdit.getSelectionStart();
		int selEnd = songContentEdit.getSelectionEnd();
		
		String before = edited.substring(0, selStart);
		String after = edited.substring(selEnd);
		
		// if there's nonempty selection
		if (selStart < selEnd) {
			
			String selected = edited.substring(selStart, selEnd);
			edited = before + "[" + selected + "]" + after;
			selStart++;
			selEnd++;
			
		} else { // just single cursor
			
			// if it's the end of line AND there is no space before
			if ((after.isEmpty() || after.startsWith("\n")) && !before.isEmpty() && !before.endsWith(" ")) {
				// insert missing space
				edited = before + " []" + after;
				selStart += 2;
			} else {
				edited = before + "[]" + after;
				selStart += 1;
			}
			selEnd = selStart;
			
		}
		
		songContentEdit.setText(edited);
		songContentEdit.setSelection(selStart, selEnd);
		songContentEdit.requestFocus();
	}
	
	private void importContentFromFile() {
		songImportFileChooser.get().showFileChooser();
	}
	
	public void setCurrentSong(Song song) {
		this.currentSong = song;
		if (currentSong == null) {
			songTitle = null;
			songContent = null;
		} else {
			songTitle = currentSong.getTitle();
			songContent = currentSong.getContent();
		}
	}
	
	private void saveSong() {
		songTitle = songTitleEdit.getText().toString();
		songContent = songContentEdit.getText().toString();
		
		if (songTitle.isEmpty() || songContent.isEmpty()) {
			uiInfoService.showInfo(R.string.fill_in_all_fields);
			return;
		}
		
		if (currentSong == null) {
			// add
			currentSong = songImportService.get().addCustomSong(songTitle, songContent);
		} else {
			// update
			songImportService.get().updateSong(currentSong, songTitle, songContent);
		}
		uiInfoService.showInfo(R.string.edit_song_has_been_saved);
		layoutController.showLastSongSelectionLayout();
	}
	
	private void removeSong() {
		if (currentSong == null) {
			// just cancel
		} else {
			// remove song from database
			songImportService.get().removeSong(currentSong);
		}
		uiInfoService.showInfo(R.string.edit_song_has_been_removed);
		layoutController.showLastSongSelectionLayout();
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.EDIT_SONG;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.edit_song;
	}
	
	@Override
	public void onBackClicked() {
		layoutController.showLastSongSelectionLayout();
	}
	
	@Override
	public void onLayoutExit() {
		softKeyboardService.hideSoftKeyboard();
	}
	
	public void setSongFromFile(String filename, String content) {
		songTitle = songTitleEdit.getText().toString();
		songContent = songContentEdit.getText().toString();
		
		if (songTitle.isEmpty()) {
			songTitle = filename;
			songTitleEdit.setText(songTitle);
		}
		
		songContent = content;
		songContentEdit.setText(songContent);
	}
}
