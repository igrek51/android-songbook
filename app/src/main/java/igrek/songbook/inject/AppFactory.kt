package igrek.songbook.inject

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.WebviewLayoutController
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.activity.AppInitializer
import igrek.songbook.activity.MainActivityData
import igrek.songbook.activity.OptionSelectDispatcher
import igrek.songbook.admin.AdminCategoryManager
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.SongRankService
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.billing.BillingService
import igrek.songbook.cast.SongCastLobbyLayout
import igrek.songbook.cast.SongCastMenuLayout
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.diagram.ChordDiagramsService
import igrek.songbook.chords.loader.LyricsLoader
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.custom.ExportFileChooser
import igrek.songbook.custom.ImportFileChooser
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.custom.share.ShareSongService
import igrek.songbook.custom.sync.EditorSessionService
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.analytics.CrashlyticsLogger
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logview.LogsLayoutController
import igrek.songbook.layout.GlobalFocusTraverser
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.ad.AdService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongsBackuper
import igrek.songbook.playlist.PlaylistFillLayoutController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.room.BluetoothService
import igrek.songbook.room.RoomListLayoutController
import igrek.songbook.room.RoomLobby
import igrek.songbook.room.RoomLobbyLayoutController
import igrek.songbook.secret.CommanderService
import igrek.songbook.secret.CommanderUtils
import igrek.songbook.send.ContactLayoutController
import igrek.songbook.send.MissingSongLayoutController
import igrek.songbook.send.PublishSongLayoutController
import igrek.songbook.send.PublishSongService
import igrek.songbook.send.SendMessageService
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.settings.buttons.MediaButtonService
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.enums.SettingsEnumService
import igrek.songbook.settings.homescreen.HomeScreenEnumService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.SettingsState
import igrek.songbook.settings.sync.BackupSyncManager
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.SongDetailsService
import igrek.songbook.songpreview.SongGestureController
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuCast
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.songpreview.scroll.ScrollService
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.favourite.FavouriteSongsService
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.random.RandomSongOpener
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.top.TopSongsLayoutController
import igrek.songbook.songselection.tree.ScrollPosBuffer
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.ClipboardManager
import igrek.songbook.system.PackageInfoService
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.SystemKeyDispatcher
import igrek.songbook.system.WindowManagerService
import okhttp3.OkHttpClient


class AppFactory(
    private var _activity: AppCompatActivity?,
) {

    val activity: LazyInject<Activity> = SingletonInject { _activity!! }
    val appCompatActivity: LazyInject<AppCompatActivity> = SingletonInject { _activity!! }

    val context: LazyInject<Context> = SingletonInject { _activity!!.applicationContext }
    val logger: LazyInject<Logger> = PrototypeInject { LoggerFactory.logger }

    /* Services */
    val activityData = SingletonInject { MainActivityData() }
    val activityController = SingletonInject { ActivityController() }
    val appInitializer = SingletonInject { AppInitializer() }
    val optionSelectDispatcher = SingletonInject { OptionSelectDispatcher() }
    val systemKeyDispatcher = SingletonInject { SystemKeyDispatcher() }
    val windowManagerService = SingletonInject { WindowManagerService() }
    val uiResourceService = SingletonInject { UiResourceService() }
    val uiInfoService = SingletonInject { UiInfoService() }
    val autoscrollService = SingletonInject { AutoscrollService() }
    val lyricsLoader = SingletonInject { LyricsLoader() }
    val preferencesService = SingletonInject { PreferencesService() }
    val scrollPosBuffer = SingletonInject { ScrollPosBuffer() }
    val layoutController = SingletonInject { LayoutController() }
    val softKeyboardService = SingletonInject { SoftKeyboardService() }
    val songTreeLayoutController = SingletonInject { SongTreeLayoutController() }
    val songPreviewLayoutController = SingletonInject { SongPreviewLayoutController() }
    val quickMenuTranspose = SingletonInject { QuickMenuTranspose() }
    val quickMenuAutoscroll = SingletonInject { QuickMenuAutoscroll() }
    val navigationMenuController = SingletonInject { NavigationMenuController() }
    val songSearchLayoutController = SingletonInject { SongSearchLayoutController() }
    val aboutLayoutController = SingletonInject { AboutLayoutController() }
    val contactLayoutController = SingletonInject { ContactLayoutController() }
    val localFilesystem = SingletonInject { LocalFilesystem() }
    val songsRepository = SingletonInject { SongsRepository() }
    val permissionService = SingletonInject { PermissionService() }
    val commanderService = SingletonInject { CommanderService() }
    val commanderUtils = SingletonInject { CommanderUtils() }
    val packageInfoService = SingletonInject { PackageInfoService() }
    val settingsLayoutController = SingletonInject { SettingsLayoutController() }
    val songDetailsService = SingletonInject { SongDetailsService() }
    val sendMessageService = SingletonInject { SendMessageService() }
    val songImportFileChooser = SingletonInject { SongImportFileChooser() }
    val exportFileChooser = SingletonInject { ExportFileChooser() }
    val okHttpClient = SingletonInject { OkHttpClient() }
    val customSongService = SingletonInject { CustomSongService() }
    val editSongLayoutController = SingletonInject { EditSongLayoutController() }
    val songsUpdater = SingletonInject { SongsUpdater() }
    val randomSongOpener = SingletonInject { RandomSongOpener() }
    val appLanguageService = SingletonInject { AppLanguageService() }
    val favouriteSongsService = SingletonInject { FavouriteSongsService() }
    val favouritesLayoutController = SingletonInject { FavouritesLayoutController() }
    val chordsNotationService = SingletonInject { ChordsNotationService() }
    val settingsState = SingletonInject { SettingsState() }
    val lyricsThemeService = SingletonInject { LyricsThemeService() }
    val customSongsListLayoutController = SingletonInject { CustomSongsListLayoutController() }
    val songContextMenuBuilder = SingletonInject { SongContextMenuBuilder() }
    val chordsEditorLayoutController = SingletonInject { ChordsEditorLayoutController() }
    val contextMenuBuilder = SingletonInject { ContextMenuBuilder() }
    val userDataDao = SingletonInject { UserDataDao() }
    val playlistLayoutController = SingletonInject { PlaylistLayoutController() }
    val playlistFillLayoutController = SingletonInject { PlaylistFillLayoutController() }
    val playlistService = SingletonInject { PlaylistService() }
    val latestSongsLayoutController = SingletonInject { LatestSongsLayoutController() }
    val topSongsLayoutController = SingletonInject { TopSongsLayoutController() }
    val songOpener = SingletonInject { SongOpener() }
    val openHistoryLayoutController = SingletonInject { OpenHistoryLayoutController() }
    val chordDiagramsService = SingletonInject { ChordDiagramsService() }
    val publishSongLayoutController = SingletonInject { PublishSongLayoutController() }
    val missingSongLayoutController = SingletonInject { MissingSongLayoutController() }
    val publishSongService = SingletonInject { PublishSongService() }
    val adminService = SingletonInject { AdminService() }
    val adminSongsLayoutContoller = SingletonInject { AdminSongsLayoutContoller() }
    val antechamberService = SingletonInject { AntechamberService() }
    val backupSyncManager = SingletonInject { BackupSyncManager() }
    val adService = SingletonInject { AdService() }
    val songRankService = SingletonInject { SongRankService() }
    val clipboardManager = SingletonInject { ClipboardManager() }
    val adminCategoryManager = SingletonInject { AdminCategoryManager() }
    val roomListLayoutController = SingletonInject { RoomListLayoutController() }
    val roomLobbyLayoutController = SingletonInject { RoomLobbyLayoutController() }
    val bluetoothService = SingletonInject { BluetoothService() }
    val roomLobby = SingletonInject { RoomLobby() }
    val importFileChooser = SingletonInject { ImportFileChooser() }
    val activityResultDispatcher = SingletonInject { ActivityResultDispatcher() }
    val shareSongService = SingletonInject { ShareSongService() }
    val mediaButtonService = SingletonInject { MediaButtonService() }
    val billingService = SingletonInject { BillingService() }
    val billingLayoutController = SingletonInject { BillingLayoutController() }
    val homeScreenEnumService = SingletonInject { HomeScreenEnumService() }
    val settingsEnumService = SingletonInject { SettingsEnumService() }
    val globalFocusTraverser = SingletonInject { GlobalFocusTraverser() }
    val webviewLayoutController = SingletonInject { WebviewLayoutController() }
    val crashlyticsLogger = SingletonInject { CrashlyticsLogger() }
    val deviceIdProvider = SingletonInject { DeviceIdProvider() }
    val editorSessionService = SingletonInject { EditorSessionService() }
    val logsLayoutController = SingletonInject { LogsLayoutController() }
    val customSongsBackuper = SingletonInject { CustomSongsBackuper() }
    val songCastMenuLayout = SingletonInject { SongCastMenuLayout() }
    val songCastLobbyLayout = SingletonInject { SongCastLobbyLayout() }
    val songCastService = SingletonInject { SongCastService() }
    val quickMenuCast = SingletonInject { QuickMenuCast() }
    val scrollService = SingletonInject { ScrollService() }
    val aSongGestureController = SingletonInject { SongGestureController() }
}
