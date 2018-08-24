package igrek.songbook.logger;

public enum LogLevel {
	
	OFF(0), // for settings only
	
	FATAL(10),
	
	ERROR(20),
	
	WARN(30),
	
	INFO(40),
	
	DEBUG(50),
	
	TRACE(60),
	
	ALL(1000); // for settings only
	
	/** lower number - higher priority (more important) */
	private int levelNumber;
	
	LogLevel(int levelNumber) {
		this.levelNumber = levelNumber;
	}
	
	public boolean moreOrEqualImportant(LogLevel than) {
		return levelNumber <= than.levelNumber;
	}
	
	public boolean lessOrEqualImportant(LogLevel than) {
		return levelNumber >= than.levelNumber;
	}
	
}
