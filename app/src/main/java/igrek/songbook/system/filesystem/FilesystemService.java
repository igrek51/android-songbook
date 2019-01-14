package igrek.songbook.system.filesystem;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

/**
 * Filesystem facade
 */
public class FilesystemService {
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	@Inject
	Activity activity;
	@Inject
	ExternalCardService externalCardService;
	
	public FilesystemService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean mkdirIfNotExist(String path) {
		File f = new File(path);
		return !f.exists() && f.mkdirs();
	}
	
	public List<String> listFilenames(String path) {
		List<File> files = listFiles(path);
		List<String> filenames = new ArrayList<>();
		for (File file : files) {
			filenames.add(file.getName());
		}
		return filenames;
	}
	
	public List<File> listFiles(String path) {
		File f = new File(path);
		List<File> files = Arrays.asList(f.listFiles());
		Collections.sort(files, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return files;
	}
	
	private byte[] openFile(String filename) throws IOException {
		RandomAccessFile f = new RandomAccessFile(new File(filename), "r");
		int length = (int) f.length();
		byte[] data = new byte[length];
		f.readFully(data);
		f.close();
		return data;
	}
	
	public String openFileString(String filename) throws IOException {
		byte[] bytes = openFile(filename);
		return new String(bytes, "UTF-8");
	}
	
	private void saveFile(String filename, byte[] data) throws IOException {
		File file = new File(filename);
		createMissingParentDir(file);
		FileOutputStream fos;
		fos = new FileOutputStream(file);
		fos.write(data);
		fos.flush();
		fos.close();
	}
	
	public void createMissingParentDir(File file) {
		File parentDir = file.getParentFile();
		if (!parentDir.exists()) {
			if (parentDir.mkdirs()) {
				logger.debug("missing dir created: " + parentDir.toString());
			}
		}
	}
	
	public void saveFile(String filename, String str) throws IOException {
		saveFile(filename, str.getBytes());
	}
	
	public void copy(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}
	
	public void ensureAppDataDirExists() {
		File externalSD = new File(externalCardService.getExternalSDPath());
		File appDataDir = new File(externalSD, "Android/data/" + activity.getPackageName());
		if (!appDataDir.exists()) {
			// WTF!?? getExternalFilesDir creates dir on SD card but returns Internal storage path
			logger.info(activity.getExternalFilesDir("data").getAbsolutePath());
			if (appDataDir.mkdirs() && appDataDir.exists()) {
				logger.debug("Android/data/package directory has been created");
			} else {
				logger.error("Failed to create Android/data/package directory");
			}
		}
	}
	
	public String trimEndSlash(String str) {
		while (str.endsWith("/"))
			str = str.substring(0, str.length() - 1);
		return str;
	}
}
