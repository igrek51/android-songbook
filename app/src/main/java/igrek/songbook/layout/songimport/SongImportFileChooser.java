package igrek.songbook.layout.songimport;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class SongImportFileChooser {
	
	private Logger logger = LoggerFactory.getLogger();
	@Inject
	Activity activity;
	@Inject
	UiInfoService uiInfoService;
	
	public static final int FILE_SELECT_CODE = 7;
	
	public SongImportFileChooser() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		try {
			activity.startActivityForResult(Intent.createChooser(intent, "Select a file to import"), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(activity, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onFileSelect(Uri selectedUri) {
		try {
			if (selectedUri != null) {
				InputStream inputStream = activity.getContentResolver()
						.openInputStream(selectedUri);
				
				int length = inputStream.available();
				
				uiInfoService.showToast("" + length);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	
}
