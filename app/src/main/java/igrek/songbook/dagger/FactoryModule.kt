package igrek.songbook.dagger


import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.about.secret.SecretCommandService
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.AppInitializer
import igrek.songbook.activity.OptionSelectDispatcher
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.contact.*
import igrek.songbook.custom.CustomSongEditLayoutController
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.CustomSongsLayoutController
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
import igrek.songbook.settings.preferences.PreferencesUpdater
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

    /* Services */

    @Provides
    @Singleton
    fun provideActivityController(): ActivityController {
        return ActivityController()
    }

    @Provides
    @Singleton
    fun provideAppInitializer(): AppInitializer {
        return AppInitializer()
    }

    @Provides
    @Singleton
    fun provideOptionSelectDispatcher(): OptionSelectDispatcher {
        return OptionSelectDispatcher()
    }

    @Provides
    @Singleton
    fun provideSystemKeyDispatcher(): SystemKeyDispatcher {
        return SystemKeyDispatcher()
    }

    @Provides
    @Singleton
    fun provideScreenService(): WindowManagerService {
        return WindowManagerService()
    }

    @Provides
    @Singleton
    fun provideUIResourceService(): UiResourceService {
        return UiResourceService()
    }

    @Provides
    @Singleton
    fun provideUserInfoService(): UiInfoService {
        return UiInfoService()
    }

    @Provides
    @Singleton
    fun provideAutoscrollService(): AutoscrollService {
        return AutoscrollService()
    }

    @Provides
    @Singleton
    fun provideChordsManager(): LyricsManager {
        return LyricsManager()
    }

    @Provides
    @Singleton
    fun providePreferencesService(): PreferencesService {
        return PreferencesService()
    }

    @Provides
    @Singleton
    fun provideChordsTransposerManager(): ChordsTransposerManager {
        return ChordsTransposerManager()
    }

    @Provides
    @Singleton
    fun provideScrollPosBuffer(): ScrollPosBuffer {
        return ScrollPosBuffer()
    }

    @Provides
    @Singleton
    fun provideLayoutController(): LayoutController {
        return LayoutController()
    }

    @Provides
    @Singleton
    fun provideSoftKeyboardService(): SoftKeyboardService {
        return SoftKeyboardService()
    }

    @Provides
    @Singleton
    fun provideSongSelectionController(): SongTreeLayoutController {
        return SongTreeLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongPreviewController(): SongPreviewLayoutController {
        return SongPreviewLayoutController()
    }

    @Provides
    @Singleton
    fun provideQuickMenu(): QuickMenuTranspose {
        return QuickMenuTranspose()
    }

    @Provides
    @Singleton
    fun provideQuickMenuAutoscroll(): QuickMenuAutoscroll {
        return QuickMenuAutoscroll()
    }

    @Provides
    @Singleton
    fun provideNavigationMenuController(): NavigationMenuController {
        return NavigationMenuController()
    }

    @Provides
    @Singleton
    fun provideSongSearchController(): SongSearchLayoutController {
        return SongSearchLayoutController()
    }

    @Provides
    @Singleton
    fun provideAboutLayoutController(): AboutLayoutController {
        return AboutLayoutController()
    }

    @Provides
    @Singleton
    fun provideContactLayoutController(): ContactLayoutController {
        return ContactLayoutController()
    }

    @Provides
    @Singleton
    fun provideHelpLayoutController(): HelpLayoutController {
        return HelpLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongsDbService(): LocalDbService {
        return LocalDbService()
    }

    @Provides
    @Singleton
    fun provideSongsRepository(): SongsRepository {
        return SongsRepository()
    }

    @Provides
    @Singleton
    fun providePermissionService(): PermissionService {
        return PermissionService()
    }

    @Provides
    @Singleton
    fun provideSecretCommandService(): SecretCommandService {
        return SecretCommandService()
    }

    @Provides
    @Singleton
    fun providePackageInfoService(): PackageInfoService {
        return PackageInfoService()
    }

    @Provides
    @Singleton
    fun provideSettingsLayoutController(): SettingsLayoutController {
        return SettingsLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongDetailsService(): SongDetailsService {
        return SongDetailsService()
    }

    @Provides
    @Singleton
    fun provideSendFeedbackService(): SendMessageService {
        return SendMessageService()
    }

    @Provides
    @Singleton
    fun provideSongImportFileChooser(): SongImportFileChooser {
        return SongImportFileChooser()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient()
    }

    @Provides
    @Singleton
    fun provideSongImportService(): CustomSongService {
        return CustomSongService()
    }

    @Provides
    @Singleton
    fun provideEditImportSongLayoutController(): CustomSongEditLayoutController {
        return CustomSongEditLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongsUpdater(): SongsUpdater {
        return SongsUpdater()
    }

    @Provides
    @Singleton
    fun provideRandomSongSelector(): RandomSongOpener {
        return RandomSongOpener()
    }

    @Provides
    @Singleton
    fun provideAppLanguageService(): AppLanguageService {
        return AppLanguageService()
    }

    @Provides
    @Singleton
    fun provideFavouriteSongService(): FavouriteSongsService {
        return FavouriteSongsService()
    }

    @Provides
    @Singleton
    fun provideFavouritesLayoutController(): FavouritesLayoutController {
        return FavouritesLayoutController()
    }

    @Provides
    @Singleton
    fun provideChordsNotationService(): ChordsNotationService {
        return ChordsNotationService()
    }

    @Provides
    @Singleton
    fun providePreferencesUpdater(): PreferencesUpdater {
        return PreferencesUpdater()
    }

    @Provides
    @Singleton
    fun provideLyricsThemeService(): LyricsThemeService {
        return LyricsThemeService()
    }

    @Provides
    @Singleton
    fun provideCustomSongsLayoutController(): CustomSongsLayoutController {
        return CustomSongsLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongContextMenuBuilder(): SongContextMenuBuilder {
        return SongContextMenuBuilder()
    }

    @Provides
    @Singleton
    fun provideChordsEditorLayoutController(): ChordsEditorLayoutController {
        return ChordsEditorLayoutController()
    }

    @Provides
    @Singleton
    fun provideContextMenuBuilder(): ContextMenuBuilder {
        return ContextMenuBuilder()
    }

    @Provides
    @Singleton
    fun provideUserDbService(): UserDataDao {
        return UserDataDao()
    }

    @Provides
    @Singleton
    fun providePlaylistLayoutController(): PlaylistLayoutController {
        return PlaylistLayoutController()
    }

    @Provides
    @Singleton
    fun providePlaylistService(): PlaylistService {
        return PlaylistService()
    }

    @Provides
    @Singleton
    fun provideLatestSongsLayoutController(): LatestSongsLayoutController {
        return LatestSongsLayoutController()
    }

    @Provides
    @Singleton
    fun provideSongOpener(): SongOpener {
        return SongOpener()
    }

    @Provides
    @Singleton
    fun provideOpenHistoryLayoutController(): OpenHistoryLayoutController {
        return OpenHistoryLayoutController()
    }

    @Provides
    @Singleton
    fun provideChordsDefinitionService(): ChordsDiagramsService {
        return ChordsDiagramsService()
    }

    @Provides
    @Singleton
    fun provideChordsInstrumentService(): ChordsInstrumentService {
        return ChordsInstrumentService()
    }

    @Provides
    @Singleton
    fun providePublishSongLayoutController(): PublishSongLayoutController {
        return PublishSongLayoutController()
    }

    @Provides
    @Singleton
    fun provideMissingSongLayoutController(): MissingSongLayoutController {
        return MissingSongLayoutController()
    }

    @Provides
    @Singleton
    fun providePublishSongService(): PublishSongService {
        return PublishSongService()
    }

    /*
	 * Empty service pattern:
	@Provides
    @Singleton
    fun provide():  {
        return ()
    }

	 */

}
