package igrek.songbook.persistence.migrator

interface IMigration {
    fun migrate(migrator: DatabaseMigrator)
}
