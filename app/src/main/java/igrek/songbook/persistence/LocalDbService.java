package igrek.songbook.persistence;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.system.PermissionService;

public class LocalDbService {
	
	@Inject
	Activity activity;
	@Inject
	PermissionService permissionService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private SQLiteDatabase songsDbHelper;
	private SQLiteDatabase customSongsDbHelper;
	private SQLiteDatabase unlockedSongsDbHelper;
	
	public LocalDbService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public SQLiteDatabase openSongsDb() {
		if (songsDbHelper == null) {
			File dbFile = getSongsDbFile();
			// if file does not exist - copy initial db from resources
			if (!dbFile.exists())
				copyFileFromResources(R.raw.songs, dbFile);
			songsDbHelper = openDatabase(dbFile);
		}
		return songsDbHelper;
	}
	
	public SQLiteDatabase openCustomSongsDb() {
		if (customSongsDbHelper == null) {
			File dbFile = getCustomSongsDbFile();
			// if file does not exist - copy initial db from resources
			if (!dbFile.exists())
				copyFileFromResources(R.raw.custom_songs, dbFile);
			customSongsDbHelper = openDatabase(dbFile);
		}
		return customSongsDbHelper;
	}
	
	public SQLiteDatabase openUnlockedSongsDb() {
		if (unlockedSongsDbHelper == null) {
			File dbFile = getUnlockedSongsDbFile();
			// if file does not exist - copy initial db from resources
			if (!dbFile.exists())
				copyFileFromResources(R.raw.unlocked_songs, dbFile);
			unlockedSongsDbHelper = openDatabase(dbFile);
		}
		return unlockedSongsDbHelper;
	}
	
	public void closeDatabases() {
		if (songsDbHelper != null) {
			songsDbHelper.close();
			songsDbHelper = null;
		}
		if (customSongsDbHelper != null) {
			customSongsDbHelper.close();
			customSongsDbHelper = null;
		}
		if (unlockedSongsDbHelper != null) {
			unlockedSongsDbHelper.close();
			unlockedSongsDbHelper = null;
		}
	}
	
	public void factoryResetDbs() {
		closeDatabases();
		// remove db files
		removeDb(getSongsDbFile());
		removeDb(getCustomSongsDbFile());
		removeDb(getUnlockedSongsDbFile());
		// need to reopen dbs again (in external dependencies)
	}
	
	private void removeDb(File songsDbFile) {
		if (songsDbFile.exists()) {
			if (!songsDbFile.delete() || songsDbFile.exists())
				logger.error("failed to delete database file: " + songsDbFile.getAbsolutePath());
		}
	}
	
	private SQLiteDatabase openDatabase(File songsDbFile) {
		if (!songsDbFile.exists())
			logger.warn("Database file does not exist: " + songsDbFile.getAbsolutePath());
		return SQLiteDatabase.openDatabase(songsDbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
	}
	
	private void copyFileFromResources(int resourceId, File targetPath) {
		byte[] buff = new byte[1024];
		try (InputStream in = activity.getResources()
				.openRawResource(resourceId); FileOutputStream out = new FileOutputStream(targetPath)) {
			int read;
			while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	@SuppressLint("SdCardPath")
	private File getSongDbDir() {
		File dir;
		
		// /data/data/PACKAGE/files
		dir = activity.getFilesDir();
		if (dir != null && dir.isDirectory())
			return dir;
		
		// INTERNAL_STORAGE/Android/data/PACKAGE/files/data
		if (permissionService.isStoragePermissionGranted()) {
			dir = activity.getExternalFilesDir("data");
			if (dir != null && dir.isDirectory())
				return dir;
		}
		
		return new File("/data/data/" + activity.getPackageName() + "/files");
	}
	
	public File getSongsDbFile() {
		return new File(getSongDbDir(), "songs.sqlite");
	}
	
	private File getCustomSongsDbFile() {
		return new File(getSongDbDir(), "custom_songs.sqlite");
	}
	
	private File getUnlockedSongsDbFile() {
		return new File(getSongDbDir(), "unlocked_songs.sqlite");
	}
	
}
