package igrek.songbook.domain.song;

public class Song {
	private String filePath;
	private String fileContent;
	private String title;
	private SongCategory category;
	private long versionNumber;
	private boolean locked;
	private String lockPassword;
}
