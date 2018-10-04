package igrek.songbook.layout.edit;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class SongImportFileChooser {
	
	private Logger logger = LoggerFactory.getLogger();
	@Inject
	Activity activity;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	Lazy<EditSongLayoutController> editSongLayoutController;
	
	public static final int FILE_SELECT_CODE = 7;
	
	public SongImportFileChooser() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		try {
			String title = uiResourceService.resString(R.string.select_file_to_import);
			activity.startActivityForResult(Intent.createChooser(intent, title), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			uiInfoService.showToast(R.string.file_manager_not_found);
		}
	}
	
	public void onFileSelect(Uri selectedUri) {
		try {
			if (selectedUri != null) {
				InputStream inputStream = activity.getContentResolver()
						.openInputStream(selectedUri);
				String filename = getFileNameFromUri(selectedUri);
				
				int length = inputStream.available();
				if (length > 50 * 1024) {
					uiInfoService.showToast(R.string.selected_file_is_too_big);
					return;
				}
				
				String content = convert(inputStream, Charset.forName("UTF-8"));
				
				editSongLayoutController.get().setSongFromFile(filename, content);
			}
		} catch (IOException | UnsupportedCharsetException e) {
			logger.error(e);
		}
	}
	
	private String convert(InputStream inputStream, Charset charset) throws IOException {
		return CharStreams.toString(new InputStreamReader(inputStream, charset));
	}
	
	private String getFileNameFromUri(Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
			try {
				if (cursor != null && cursor.moveToFirst()) {
					result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			} finally {
				cursor.close();
			}
		}
		if (result == null) {
			result = uri.getPath();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}
}
