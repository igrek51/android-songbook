package igrek.songbook.service.persistence.database;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class LocalDatabaseService {
	
	@Inject
	Activity activity;
	
	private SQLiteDbHelper dbHelper;
	private Logger logger = LoggerFactory.getLogger();
	
	public LocalDatabaseService() {
		DaggerIoc.getFactoryComponent().inject(this);
		
		// if file does not exist - copy initial db from resources
		File songsDbFile = getSongsDbFile();
		if (!songsDbFile.exists()) {
			createInitialDb(songsDbFile);
		}
		
		dbHelper = new SQLiteDbHelper(activity, songsDbFile.getAbsolutePath());
	}
	
	private void createInitialDb(File dbFile) {
		logger.info("no database file - creating intial db from resources");
		copyFileFromResources(R.raw.songs, dbFile);
	}
	
	private void copyFileFromResources(int resourceId, File targetPath) {
		byte[] buff = new byte[1024];
		try (InputStream in = activity.getResources()
				.openRawResource(resourceId); FileOutputStream out = new FileOutputStream(targetPath)) {
			int read;
			while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	@SuppressLint("SdCardPath")
	private File getSongDbDir() {
		File dir = activity.getExternalFilesDir("data");
		if (dir != null && dir.isDirectory())
			return dir;
		
		dir = activity.getFilesDir();
		if (dir != null && dir.isDirectory())
			return dir;
		
		return new File("/data/data/" + activity.getPackageName() + "/files");
	}
	
	private File getSongsDbFile() {
		return new File(getSongDbDir(), "songs.sqlite");
	}
	
	public SQLiteDbHelper getDbHelper() {
		return dbHelper;
	}
}
