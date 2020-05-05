package igrek.songbook.activity

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.custom.SongExportFileChooser
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SystemKeyDispatcher

/*
    Main Activity starter pack
    Workaround for reusing finished activities by Android
 */
class MainActivityData(
        appInitializer: LazyInject<AppInitializer> = appFactory.appInitializer,
        activityController: LazyInject<ActivityController> = appFactory.activityController,
        optionSelectDispatcher: LazyInject<OptionSelectDispatcher> = appFactory.optionSelectDispatcher,
        systemKeyDispatcher: LazyInject<SystemKeyDispatcher> = appFactory.systemKeyDispatcher,
        permissionService: LazyInject<PermissionService> = appFactory.permissionService,
        songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
        songExportFileChooser: LazyInject<SongExportFileChooser> = appFactory.songExportFileChooser,
        googleSyncManager: LazyInject<GoogleSyncManager> = appFactory.googleSyncManager,
) : AppCompatActivity() {
    var appInitializer by LazyExtractor(appInitializer)
    var activityController by LazyExtractor(activityController)
    var optionSelectDispatcher by LazyExtractor(optionSelectDispatcher)
    var systemKeyDispatcher by LazyExtractor(systemKeyDispatcher)
    var permissionService by LazyExtractor(permissionService)
    var songImportFileChooser by LazyExtractor(songImportFileChooser)
    var songExportFileChooser by LazyExtractor(songExportFileChooser)
    var googleSyncManager by LazyExtractor(googleSyncManager)
}