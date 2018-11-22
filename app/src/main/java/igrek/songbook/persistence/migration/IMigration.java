package igrek.songbook.persistence.migration;

public interface IMigration {
	void migrate(DatabaseMigrator migrator);
}
