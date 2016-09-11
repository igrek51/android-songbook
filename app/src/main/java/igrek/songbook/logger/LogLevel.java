package igrek.songbook.logger;

public enum LogLevel {

    OFF(0), //tylko do konfiguracji poziomów

    FATAL(10),

    ERROR(20),

    WARN(30),

    INFO(40),

    DEBUG(50),

    TRACE(60),

    ALL(1000); //tylko do konfiguracji poziomów

    /** mniejszy numer poziomu - ważniejszy */
    private int levelNumber;

    LogLevel(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public boolean lower(LogLevel level2) {
        return levelNumber < level2.levelNumber;
    }

    public boolean lowerOrEqual(LogLevel level2) {
        return levelNumber <= level2.levelNumber;
    }

    public boolean higher(LogLevel level2) {
        return levelNumber > level2.levelNumber;
    }

    public boolean higherOrEqual(LogLevel level2) {
        return levelNumber >= level2.levelNumber;
    }

}
