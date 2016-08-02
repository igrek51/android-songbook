package igrek.songbook.output;

public enum LogLevel {

    OFF(0), //tylko do konfiguracji poziomów

    CRITICAL_ERROR(1),

    ERROR(2),

    WARN(3),

    INFO(4),

    DEBUG(5),

    ALL(100); //tylko do konfiguracji poziomów

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
