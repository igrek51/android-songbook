package igrek.songbook.dagger

import dagger.Component
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.about.secret.SecretCommandService
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.AppInitializer
import igrek.songbook.activity.MainActivity
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
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.errorcheck.UIErrorHandler
import igrek.songbook.layout.InflatedLayout
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.layout.navigation.TitleBarView
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.general.dao.PublicSongsDao
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.preferences.PreferencesDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.SettingsFragment
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.preferences.SharedPreferencesService
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.SongDetailsService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.lyrics.LyricsManager
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.songselection.SongListItemAdapter
import igrek.songbook.songselection.SongListView
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
import javax.inject.Singleton

/**
 * Dagger will be injecting to those classes
 */
@Singleton
@Component(modules = [FactoryModule::class])
interface FactoryComponent {

    fun inject(there: MainActivity)
    fun inject(there: AppInitializer)
    fun inject(there: ActivityController)
    fun inject(there: WindowManagerService)
    fun inject(there: OptionSelectDispatcher)
    fun inject(there: UiResourceService)
    fun inject(there: UiInfoService)
    fun inject(there: AutoscrollService)
    fun inject(there: LyricsManager)
    fun inject(there: PreferencesService)
    fun inject(there: ChordsTransposerManager)
    fun inject(there: SoftKeyboardService)
    fun inject(there: LayoutController)
    fun inject(there: SongTreeLayoutController)
    fun inject(there: SongPreviewLayoutController)
    fun inject(there: ScrollPosBuffer)
    fun inject(there: SystemKeyDispatcher)
    fun inject(there: NavigationMenuController)
    fun inject(there: SongSearchLayoutController)
    fun inject(there: AboutLayoutController)
    fun inject(there: ContactLayoutController)
    fun inject(there: HelpLayoutController)
    fun inject(there: LocalDbService)
    fun inject(there: SongsRepository)
    fun inject(there: PermissionService)
    fun inject(there: SecretCommandService)
    fun inject(there: PackageInfoService)
    fun inject(there: SettingsLayoutController)
    fun inject(there: SongDetailsService)
    fun inject(there: SendMessageService)
    fun inject(there: SongImportFileChooser)
    fun inject(there: PublicSongsDao)
    fun inject(there: CustomSongService)
    fun inject(there: EditSongLayoutController)
    fun inject(there: SongsUpdater)
    fun inject(there: RandomSongOpener)
    fun inject(there: AppLanguageService)
    fun inject(there: FavouriteSongsService)
    fun inject(there: FavouritesLayoutController)
    fun inject(there: SettingsFragment)
    fun inject(there: ChordsNotationService)
    fun inject(there: PreferencesState)
    fun inject(there: LyricsThemeService)
    fun inject(there: CustomSongsLayoutController)
    fun inject(there: SongContextMenuBuilder)
    fun inject(there: ChordsEditorLayoutController)
    fun inject(there: ContextMenuBuilder)
    fun inject(there: UserDataDao)
    fun inject(there: SongListItemAdapter)
    fun inject(there: QuickMenuAutoscroll)
    fun inject(there: UIErrorHandler)
    fun inject(there: SongListView)
    fun inject(there: SafeExecutor)
    fun inject(there: QuickMenuTranspose)
    fun inject(there: SongPreview)
    fun inject(there: ConfirmDialogBuilder)
    fun inject(there: CustomSongsDao)
    fun inject(there: FavouriteSongsDao)
    fun inject(there: UnlockedSongsDao)
    fun inject(there: PlaylistDao)
    fun inject(there: PlaylistLayoutController)
    fun inject(there: InflatedLayout)
    fun inject(there: InputDialogBuilder)
    fun inject(there: PlaylistService)
    fun inject(there: LatestSongsLayoutController)
    fun inject(there: OpenHistoryDao)
    fun inject(there: SongOpener)
    fun inject(there: OpenHistoryLayoutController)
    fun inject(there: ExclusionDao)
    fun inject(there: TransposeDao)
    fun inject(there: ChordsDiagramsService)
    fun inject(there: ChordsInstrumentService)
    fun inject(there: TitleBarView)
    fun inject(there: PublishSongLayoutController)
    fun inject(there: MissingSongLayoutController)
    fun inject(there: PublishSongService)
    fun inject(there: AdminService)
    fun inject(there: AdminSongsLayoutContoller)
    fun inject(there: AntechamberService)
    fun inject(there: GoogleSyncManager)
    fun inject(there: SharedPreferencesService)
    fun inject(there: PreferencesDao)

}