package igrek.songbook.dagger


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.about.secret.SecretCommandService
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.AppInitializer
import igrek.songbook.activity.OptionSelectDispatcher
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.contact.*
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.CustomSongsLayoutController
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.custom.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.SongDetailsService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.lyrics.LyricsManager
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.random.RandomSongOpener
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.tree.ScrollPosBuffer
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Module with providers. These classes can be injected
 */
@Module
open class FactoryModule(private val activity: AppCompatActivity) {

    @Provides
    fun provideContext(): Context {
        return activity.applicationContext
    }

    @Provides
    fun provideActivity(): Activity {
        return activity
    }

    @Provides
    fun provideAppCompatActivity(): AppCompatActivity {
        return activity
    }

    @Provides
    open fun provideLogger(): Logger {
        return LoggerFactory.logger
    }

    @Provides
    fun aSharedPreferences(activity: AppCompatActivity): SharedPreferences {
        return activity.applicationContext.getSharedPreferences(PreferencesService.sharedPreferencesName, Context.MODE_PRIVATE)
    }

    /* Services */

    @Provides
    @Singleton
    fun aActivityController(): ActivityController = ActivityController()

    @Provides
    @Singleton
    fun aAppInitializer(): AppInitializer = AppInitializer()

    @Provides
    @Singleton
    fun aOptionSelectDispatcher(): OptionSelectDispatcher = OptionSelectDispatcher()

    @Provides
    @Singleton
    fun aSystemKeyDispatcher(): SystemKeyDispatcher = SystemKeyDispatcher()

    @Provides
    @Singleton
    fun aScreenService(): WindowManagerService = WindowManagerService()

    @Provides
    @Singleton
    fun aUIResourceService(): UiResourceService = UiResourceService()

    @Provides
    @Singleton
    fun aUserInfoService(): UiInfoService = UiInfoService()

    @Provides
    @Singleton
    fun aAutoscrollService(): AutoscrollService = AutoscrollService()

    @Provides
    @Singleton
    fun aChordsManager(): LyricsManager = LyricsManager()

    @Provides
    @Singleton
    fun aPreferencesService(): PreferencesService = PreferencesService()

    @Provides
    @Singleton
    fun aChordsTransposerManager(): ChordsTransposerManager = ChordsTransposerManager()

    @Provides
    @Singleton
    fun aScrollPosBuffer(): ScrollPosBuffer = ScrollPosBuffer()

    @Provides
    @Singleton
    fun aLayoutController(): LayoutController = LayoutController()

    @Provides
    @Singleton
    fun aSoftKeyboardService(): SoftKeyboardService = SoftKeyboardService()

    @Provides
    @Singleton
    fun aSongSelectionController(): SongTreeLayoutController = SongTreeLayoutController()

    @Provides
    @Singleton
    fun aSongPreviewController(): SongPreviewLayoutController = SongPreviewLayoutController()

    @Provides
    @Singleton
    fun aQuickMenu(): QuickMenuTranspose = QuickMenuTranspose()

    @Provides
    @Singleton
    fun aQuickMenuAutoscroll(): QuickMenuAutoscroll = QuickMenuAutoscroll()

    @Provides
    @Singleton
    fun aNavigationMenuController(): NavigationMenuController = NavigationMenuController()

    @Provides
    @Singleton
    fun aSongSearchController(): SongSearchLayoutController = SongSearchLayoutController()

    @Provides
    @Singleton
    fun aAboutLayoutController(): AboutLayoutController = AboutLayoutController()

    @Provides
    @Singleton
    fun aContactLayoutController(): ContactLayoutController = ContactLayoutController()

    @Provides
    @Singleton
    fun aHelpLayoutController(): HelpLayoutController = HelpLayoutController()

    @Provides
    @Singleton
    fun aSongsDbService(): LocalDbService = LocalDbService()

    @Provides
    @Singleton
    fun aSongsRepository(): SongsRepository = SongsRepository()

    @Provides
    @Singleton
    fun aPermissionService(): PermissionService = PermissionService()

    @Provides
    @Singleton
    fun aSecretCommandService(): SecretCommandService = SecretCommandService()

    @Provides
    @Singleton
    fun aPackageInfoService(): PackageInfoService = PackageInfoService()

    @Provides
    @Singleton
    fun aSettingsLayoutController(): SettingsLayoutController = SettingsLayoutController()

    @Provides
    @Singleton
    fun aSongDetailsService(): SongDetailsService = SongDetailsService()

    @Provides
    @Singleton
    fun aSendFeedbackService(): SendMessageService = SendMessageService()

    @Provides
    @Singleton
    fun aSongImportFileChooser(): SongImportFileChooser = SongImportFileChooser()

    @Provides
    @Singleton
    fun aOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun aSongImportService(): CustomSongService = CustomSongService()

    @Provides
    @Singleton
    fun aEditImportSongLayoutController(): EditSongLayoutController = EditSongLayoutController()

    @Provides
    @Singleton
    fun aSongsUpdater(): SongsUpdater = SongsUpdater()

    @Provides
    @Singleton
    fun aRandomSongSelector(): RandomSongOpener = RandomSongOpener()

    @Provides
    @Singleton
    fun aAppLanguageService(): AppLanguageService = AppLanguageService()

    @Provides
    @Singleton
    fun aFavouriteSongService(): FavouriteSongsService = FavouriteSongsService()

    @Provides
    @Singleton
    fun aFavouritesLayoutController(): FavouritesLayoutController = FavouritesLayoutController()

    @Provides
    @Singleton
    fun aChordsNotationService(): ChordsNotationService = ChordsNotationService()

    @Provides
    @Singleton
    fun aPreferencesState(): PreferencesState = PreferencesState()

    @Provides
    @Singleton
    fun aLyricsThemeService(): LyricsThemeService = LyricsThemeService()

    @Provides
    @Singleton
    fun aCustomSongsLayoutController(): CustomSongsLayoutController = CustomSongsLayoutController()

    @Provides
    @Singleton
    fun aSongContextMenuBuilder(): SongContextMenuBuilder = SongContextMenuBuilder()

    @Provides
    @Singleton
    fun aChordsEditorLayoutController(): ChordsEditorLayoutController = ChordsEditorLayoutController()

    @Provides
    @Singleton
    fun aContextMenuBuilder(): ContextMenuBuilder = ContextMenuBuilder()

    @Provides
    @Singleton
    fun aUserDbService(): UserDataDao = UserDataDao()

    @Provides
    @Singleton
    fun aPlaylistLayoutController(): PlaylistLayoutController = PlaylistLayoutController()

    @Provides
    @Singleton
    fun aPlaylistService(): PlaylistService = PlaylistService()

    @Provides
    @Singleton
    fun aLatestSongsLayoutController(): LatestSongsLayoutController = LatestSongsLayoutController()

    @Provides
    @Singleton
    fun aSongOpener(): SongOpener = SongOpener()

    @Provides
    @Singleton
    fun aOpenHistoryLayoutController(): OpenHistoryLayoutController = OpenHistoryLayoutController()

    @Provides
    @Singleton
    fun aChordsDefinitionService(): ChordsDiagramsService = ChordsDiagramsService()

    @Provides
    @Singleton
    fun aChordsInstrumentService(): ChordsInstrumentService = ChordsInstrumentService()

    @Provides
    @Singleton
    fun aPublishSongLayoutController(): PublishSongLayoutController = PublishSongLayoutController()

    @Provides
    @Singleton
    fun aMissingSongLayoutController(): MissingSongLayoutController = MissingSongLayoutController()

    @Provides
    @Singleton
    fun aPublishSongService(): PublishSongService = PublishSongService()

    @Provides
    @Singleton
    fun aAdminService(): AdminService = AdminService()

    @Provides
    @Singleton
    fun aAdminSongsLayoutContoller(): AdminSongsLayoutContoller = AdminSongsLayoutContoller()

    @Provides
    @Singleton
    fun aAntechamberService(): AntechamberService = AntechamberService()

    @Provides
    @Singleton
    fun aGoogleSyncManager(): GoogleSyncManager = GoogleSyncManager()

    /*
	 * Empty service pattern:
	@Provides
    @Singleton
    fun a():  = ()

	 */

}
