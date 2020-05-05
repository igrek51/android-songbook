package igrek.songbook.inject

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.about.secret.SecretCommandService
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.AppInitializer
import igrek.songbook.activity.OptionSelectDispatcher
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.admin.antechamber.SongRankService
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.chords.lyrics.LyricsLoader
import igrek.songbook.chords.transpose.ChordsTransposerManager
import igrek.songbook.custom.*
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.ad.AdService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.send.*
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
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
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
import igrek.songbook.system.*
import okhttp3.OkHttpClient


class AppFactory(
        activity: AppCompatActivity,
) {
    val activity: LazyInject<Activity> = SingletonInject { activity }
    val appCompatActivity: LazyInject<AppCompatActivity> = SingletonInject { activity }

    val context: LazyInject<Context> = SingletonInject { activity.applicationContext }
    val logger: LazyInject<Logger> = PrototypeInject { LoggerFactory.logger }
    val sharedPreferences: LazyInject<SharedPreferences> = PrototypeInject { SharedPreferencesService.sharedPreferencesCreator(activity) }

    /* Services */
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
    val chordsTransposerManager = SingletonInject { ChordsTransposerManager() }
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
    val helpLayoutController = SingletonInject { HelpLayoutController() }
    val localDbService = SingletonInject { LocalDbService() }
    val songsRepository = SingletonInject { SongsRepository() }
    val permissionService = SingletonInject { PermissionService() }
    val secretCommandService = SingletonInject { SecretCommandService() }
    val packageInfoService = SingletonInject { PackageInfoService() }
    val settingsLayoutController = SingletonInject { SettingsLayoutController() }
    val songDetailsService = SingletonInject { SongDetailsService() }
    val sendMessageService = SingletonInject { SendMessageService() }
    val songImportFileChooser = SingletonInject { SongImportFileChooser() }
    val songExportFileChooser = SingletonInject { SongExportFileChooser() }
    val okHttpClient = SingletonInject { OkHttpClient() }
    val customSongService = SingletonInject { CustomSongService() }
    val editSongLayoutController = SingletonInject { EditSongLayoutController() }
    val songsUpdater = SingletonInject { SongsUpdater() }
    val randomSongOpener = SingletonInject { RandomSongOpener() }
    val appLanguageService = SingletonInject { AppLanguageService() }
    val favouriteSongsService = SingletonInject { FavouriteSongsService() }
    val favouritesLayoutController = SingletonInject { FavouritesLayoutController() }
    val chordsNotationService = SingletonInject { ChordsNotationService() }
    val preferencesState = SingletonInject { PreferencesState() }
    val lyricsThemeService = SingletonInject { LyricsThemeService() }
    val customSongsListLayoutController = SingletonInject { CustomSongsListLayoutController() }
    val songContextMenuBuilder = SingletonInject { SongContextMenuBuilder() }
    val chordsEditorLayoutController = SingletonInject { ChordsEditorLayoutController() }
    val contextMenuBuilder = SingletonInject { ContextMenuBuilder() }
    val userDataDao = SingletonInject { UserDataDao() }
    val playlistLayoutController = SingletonInject { PlaylistLayoutController() }
    val playlistService = SingletonInject { PlaylistService() }
    val latestSongsLayoutController = SingletonInject { LatestSongsLayoutController() }
    val topSongsLayoutController = SingletonInject { TopSongsLayoutController() }
    val songOpener = SingletonInject { SongOpener() }
    val openHistoryLayoutController = SingletonInject { OpenHistoryLayoutController() }
    val chordsDiagramsService = SingletonInject { ChordsDiagramsService() }
    val chordsInstrumentService = SingletonInject { ChordsInstrumentService() }
    val publishSongLayoutController = SingletonInject { PublishSongLayoutController() }
    val missingSongLayoutController = SingletonInject { MissingSongLayoutController() }
    val publishSongService = SingletonInject { PublishSongService() }
    val adminService = SingletonInject { AdminService() }
    val adminSongsLayoutContoller = SingletonInject { AdminSongsLayoutContoller() }
    val antechamberService = SingletonInject { AntechamberService() }
    val googleSyncManager = SingletonInject { GoogleSyncManager() }
    val sharedPreferencesService = SingletonInject { SharedPreferencesService() }
    val adService = SingletonInject { AdService() }
    val songRankService = SingletonInject { SongRankService() }
    val clipboardManager = SingletonInject { ClipboardManager() }
}
