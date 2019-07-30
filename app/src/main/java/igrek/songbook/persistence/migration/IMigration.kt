package igrek.songbook.persistence.migration

interface IMigration {
    fun migrate(migrator: DatabaseMigrator)
}
