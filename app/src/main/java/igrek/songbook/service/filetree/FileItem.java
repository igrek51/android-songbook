package igrek.songbook.service.filetree;

public class FileItem {
	
	private String filename;
	private boolean directory;
	
	private FileItem(String filename, boolean directory) {
		this.filename = filename;
		this.directory = directory;
	}
	
	public static FileItem file(String filename) {
		return new FileItem(filename, false);
	}
	
	public static FileItem directory(String filename) {
		return new FileItem(filename, true);
	}
	
	
	public String getName() {
		return filename;
	}
	
	public boolean isDirectory() {
		return directory;
	}
	
	public boolean isRegularFile() {
		return !directory;
	}
	
	@Override
	public String toString() {
		if (isDirectory()) {
			return "[" + filename + "]";
		} else {
			return filename;
		}
	}
}
