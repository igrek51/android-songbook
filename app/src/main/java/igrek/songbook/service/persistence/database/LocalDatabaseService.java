package igrek.songbook.service.persistence.database;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.exception.WrongDbVersionException;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.filesystem.PermissionService;

public class LocalDatabaseService {
	
	@Inject
	Activity activity;
	@Inject
	PermissionService permissionService;
	@Inject
	Lazy<SqlQueryService> sqlQueryService;
	
	private SQLiteDbHelper dbHelper;
	private Logger logger = LoggerFactory.getLogger();
	
	public LocalDatabaseService() {
		DaggerIoc.getFactoryComponent().inject(this);
		
		// if file does not exist - copy initial db from resources
		File songsDbFile = getSongsDbFile();
		if (!songsDbFile.exists()) {
			createInitialDb(songsDbFile);
		}
		
		initDbHelper(songsDbFile);
	}
	
	private void initDbHelper(File songsDbFile) {
		dbHelper = new SQLiteDbHelper(activity, songsDbFile.getAbsolutePath());
	}
	
	private void createInitialDb(File dbFile) {
		logger.info("recreating intial database from resources");
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
		File dir;
		// INTERNAL_STORAGE/Android/data/PACKAGE/files/data
		if (permissionService.isStoragePermissionGranted()) {
			dir = activity.getExternalFilesDir("data");
			if (dir != null && dir.isDirectory())
				return dir;
		}
		
		// /data/data/PACKAGE/files
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
	
	public void closeDatabase() {
		if (dbHelper != null)
			dbHelper.close();
	}
	
	public void recreateDb() {
		closeDatabase();
		
		File songsDbFile = getSongsDbFile();
		if (!songsDbFile.delete() || songsDbFile.exists())
			logger.error("failed to delete old database");
		
		createInitialDb(songsDbFile);
		
		initDbHelper(songsDbFile);
	}
	
	public void checkDatabaseValid() {
		try {
			long versionNumber = sqlQueryService.get().readDbVersionNumber();
			if (versionNumber < 1) {
				throw new WrongDbVersionException("db version too small: " + versionNumber);
			}
		} catch (SQLiteException | WrongDbVersionException e) {
			logger.warn("database is invalid - recreating: " + e.getMessage());
			recreateDb();
		}
	}
}
