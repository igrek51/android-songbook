package igrek.songbook.output;

public enum LogLevel {

    OFF(0), //wyłączony

    ERROR(1), //ERROR

    WARN(2), //WARN + ERROR

    INFO(3), //INFO + WARN + ERROR

    DEBUG(4); //wszystkie

    private int levelNumber;

    LogLevel(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public int getLevelNumber() {
        return levelNumber;
    }
}
