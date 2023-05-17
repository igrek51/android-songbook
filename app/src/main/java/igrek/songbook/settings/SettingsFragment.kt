package igrek.songbook.settings

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentTransaction
import androidx.preference.*
import igrek.songbook.R
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.chords.diagram.guitar.ChordDiagramStyle
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.RetryDelayed
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.user.custom.CustomSongsBackuper
import igrek.songbook.settings.buttons.MediaButtonBehaviours
import igrek.songbook.settings.buttons.MediaButtonService
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.enums.ChordsInstrument
import igrek.songbook.settings.enums.CustomSongsOrdering
import igrek.songbook.settings.enums.SettingsEnumService
import igrek.songbook.settings.homescreen.HomeScreenEnum
import igrek.songbook.settings.homescreen.HomeScreenEnumService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.sync.BackupSyncManager
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.system.LinkOpener
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class SettingsFragment(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    backupSyncManager: LazyInject<BackupSyncManager> = appFactory.backupSyncManager,
    mediaButtonService: LazyInject<MediaButtonService> = appFactory.mediaButtonService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    homeScreenEnumService: LazyInject<HomeScreenEnumService> = appFactory.homeScreenEnumService,
    settingsEnumService: LazyInject<SettingsEnumService> = appFactory.settingsEnumService,
    customSongsBackuper: LazyInject<CustomSongsBackuper> = appFactory.customSongsBackuper,
) : PreferenceFragmentCompat() {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val backupSyncManager by LazyExtractor(backupSyncManager)
    private val mediaButtonService by LazyExtractor(mediaButtonService)
    private val layoutController by LazyExtractor(layoutController)
    private val homeScreenEnumService by LazyExtractor(homeScreenEnumService)
    private val settingsEnumService by LazyExtractor(settingsEnumService)
    private val customSongsBackuper by LazyExtractor(customSongsBackuper)

    private var decimalFormat1: DecimalFormat = DecimalFormat("#.#")
    private var decimalFormat3: DecimalFormat = DecimalFormat("#.###")

    companion object {
        const val SEEKBAR_RESOLUTION = 10000
    }

    init {
        decimalFormat1.roundingMode = RoundingMode.HALF_UP
        decimalFormat3.roundingMode = RoundingMode.HALF_UP
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_def, rootKey)
        lateInit()
        Handler(Looper.getMainLooper()).post {
            lateInit()
        }
    }

    private fun lateInit() {

        setupListPreference("applicationLanguage",
            appLanguageService.languageStringEntries(),
            onLoad = { preferencesState.appLanguage.langCode },
            onSave = { id: String ->
                preferencesState.appLanguage = AppLanguage.parseByLangCode(id)
                    ?: AppLanguage.DEFAULT
                appLanguageService.updateLocale()
                uiInfoService.showToast(R.string.restart_needed)
            }
        )

        setupListPreference("chordsInstrument",
            settingsEnumService.instrumentEntries(),
            onLoad = { preferencesState.chordsInstrument.id.toString() },
            onSave = { id: String ->
                preferencesState.chordsInstrument = ChordsInstrument.parseById(id.toLong())
                    ?: ChordsInstrument.default
            }
        )

        setupListPreference("chordDiagramStyle",
            settingsEnumService.chordDiagramStyleEntries(),
            onLoad = { preferencesState.chordDiagramStyle.id.toString() },
            onSave = { id: String ->
                preferencesState.chordDiagramStyle = ChordDiagramStyle.mustParseById(id.toLong())
            }
        )

        setupListPreference("chordsNotation",
            chordsNotationService.chordsNotationEntries(),
            onLoad = { preferencesState.chordsNotation.id.toString() },
            onSave = { id: String ->
                preferencesState.chordsNotation = ChordsNotation.parseById(id.toLong())
                    ?: ChordsNotation.default
            }
        )

        setupListPreference("chordsDisplayStyle",
            lyricsThemeService.displayStyleEntries(),
            onLoad = { preferencesState.chordsDisplayStyle.id.toString() },
            onSave = { id: String ->
                preferencesState.chordsDisplayStyle = DisplayStyle.mustParseById(id.toLong())
            }
        )

        setupListPreference("fontTypeface",
            lyricsThemeService.fontTypefaceEntries(),
            onLoad = { preferencesState.fontTypeface.id },
            onSave = { id: String ->
                preferencesState.fontTypeface = FontTypeface.parseById(id)
                    ?: FontTypeface.default
            }
        )

        setupListPreference("chordsEditorFontTypeface",
            lyricsThemeService.fontTypefaceEntries(),
            onLoad = { preferencesState.chordsEditorFontTypeface.id },
            onSave = { id: String ->
                preferencesState.chordsEditorFontTypeface = FontTypeface.parseById(id)
                    ?: FontTypeface.MONOSPACE
            }
        )

        setupListPreference("colorScheme",
            lyricsThemeService.colorSchemeEntries(),
            onLoad = { preferencesState.colorScheme.id.toString() },
            onSave = { id: String ->
                preferencesState.colorScheme = ColorScheme.parseById(id.toLong())
                    ?: ColorScheme.default
            }
        )

        setupSeekBarPreference("autoscrollSpeed",
            min = AutoscrollService.MIN_SPEED,
            max = AutoscrollService.MAX_SPEED,
            onLoad = { preferencesState.autoscrollSpeed },
            onSave = { value: Float ->
                preferencesState.autoscrollSpeed = value
            },
            stringConverter = { value: Float ->
                uiInfoService.resString(
                    R.string.settings_autoscroll_speed_value,
                    decimal3(value)
                )
            }
        )

        setupSeekBarPreference("fontSize", min = 5, max = 100,
            onLoad = { preferencesState.fontsize },
            onSave = { value: Float ->
                preferencesState.fontsize = value
            },
            stringConverter = { value: Float ->
                uiInfoService.resString(R.string.settings_font_size_value, decimal1(value))
            }
        )

        setupSwitchPreference("autoscrollSpeedAutoAdjustment",
            onLoad = { preferencesState.autoscrollSpeedAutoAdjustment },
            onSave = { value: Boolean ->
                preferencesState.autoscrollSpeedAutoAdjustment = value
            }
        )

        setupSwitchPreference("autoscrollShowEyeFocus",
            onLoad = { preferencesState.autoscrollShowEyeFocus },
            onSave = { value: Boolean ->
                preferencesState.autoscrollShowEyeFocus = value
            }
        )

        setupSwitchPreference("autoscrollIndividualSpeed",
            onLoad = { preferencesState.autoscrollIndividualSpeed },
            onSave = { value: Boolean ->
                preferencesState.autoscrollIndividualSpeed = value
            }
        )

        setupSwitchPreference("autoscrollSpeedVolumeKeys",
            onLoad = { preferencesState.autoscrollSpeedVolumeKeys },
            onSave = { value: Boolean ->
                preferencesState.autoscrollSpeedVolumeKeys = value
            }
        )

        setupSwitchPreference("randomFavouriteSongsOnly",
            onLoad = { preferencesState.randomFavouriteSongsOnly },
            onSave = { value: Boolean ->
                preferencesState.randomFavouriteSongsOnly = value
            }
        )

        setupSwitchPreference("randomPlaylistSongs",
            onLoad = { preferencesState.randomPlaylistSongs },
            onSave = { value: Boolean ->
                preferencesState.randomPlaylistSongs = value
            }
        )

        setupMultiListPreference("filterLanguages",
            appLanguageService.languageFilterEntries(),
            onLoad = {
                appLanguageService.selectedSongLanguages.map { it.langCode }.toMutableSet()
            },
            onSave = { ids: Set<String> ->
                appLanguageService.setSelectedSongLanguageCodes(ids)
            },
            stringConverter = { ids: Set<String>, entriesMap: LinkedHashMap<String, String> ->
                if (ids.isEmpty())
                    uiInfoService.resString(R.string.none)
                else
                    ids.map { id -> entriesMap[id].orEmpty() }.sorted()
                        .joinToString(separator = ", ")
            }
        )

        setupSwitchPreference("restoreTransposition",
            onLoad = { preferencesState.restoreTransposition },
            onSave = { value: Boolean ->
                preferencesState.restoreTransposition = value
            }
        )

        setupClickPreference("settingsSyncSave") {
            safeExecute {
                backupSyncManager.makeDriveBackupUI()
            }
        }
        setupClickPreference("settingsSyncSaveFile") {
            safeExecute {
                backupSyncManager.makeCompositeFileBackupUI()
            }
        }
        setupClickPreference("settingsSyncRestore") {
            ConfirmDialogBuilder().confirmAction(R.string.settings_sync_restore_confirm) {
                safeExecute {
                    backupSyncManager.restoreDriveBackupUI()
                }
            }
        }
        setupClickPreference("settingsSyncRestoreFile") {
            ConfirmDialogBuilder().confirmAction(R.string.settings_sync_restore_confirm_file) {
                safeExecute {
                    backupSyncManager.restoreCompositeFileBackupUI()
                }
            }
        }

        setupClickPreference("settingsPrivacyPolicy") {
            LinkOpener().openPrivacyPolicy()
        }

        setupSwitchPreference("anonymousUsageData",
            onLoad = { preferencesState.anonymousUsageData },
            onSave = { value: Boolean ->
                preferencesState.anonymousUsageData = value
            }
        )

        setupSwitchPreference("keepScreenOn",
            onLoad = { preferencesState.keepScreenOn },
            onSave = { value: Boolean ->
                preferencesState.keepScreenOn = value
            }
        )

        setupSwitchPreference("updateDbOnStartup",
            onLoad = { preferencesState.updateDbOnStartup },
            onSave = { value: Boolean ->
                preferencesState.updateDbOnStartup = value
            }
        )

        setupSwitchPreference("trimWhitespaces",
            onLoad = { preferencesState.trimWhitespaces },
            onSave = { value: Boolean ->
                preferencesState.trimWhitespaces = value
            }
        )

        setupSwitchPreference("autoscrollAutostart",
            onLoad = { preferencesState.autoscrollAutostart },
            onSave = { value: Boolean ->
                preferencesState.autoscrollAutostart = value
            }
        )

        setupSwitchPreference("autoscrollForwardNextSong",
            onLoad = { preferencesState.autoscrollForwardNextSong },
            onSave = { value: Boolean ->
                preferencesState.autoscrollForwardNextSong = value
            }
        )

        setupSwitchPreference("horizontalScroll",
            onLoad = { preferencesState.horizontalScroll },
            onSave = { value: Boolean ->
                preferencesState.horizontalScroll = value
            }
        )

        setupSwitchPreference("forceSharpChords",
            onLoad = { preferencesState.forceSharpNotes },
            onSave = { value: Boolean ->
                preferencesState.forceSharpNotes = value
            }
        )

        setupSwitchPreference("songLyricsSearch",
            onLoad = { preferencesState.songLyricsSearch },
            onSave = { value: Boolean ->
                preferencesState.songLyricsSearch = value
            }
        )

        setupSwitchPreference("saveCustomSongsBackups",
            onLoad = { preferencesState.saveCustomSongsBackups },
            onSave = { value: Boolean ->
                preferencesState.saveCustomSongsBackups = value
            }
        )

        setupSwitchPreference("syncBackupAutomatically",
            onLoad = { preferencesState.syncBackupAutomatically },
            onSave = { value: Boolean ->
                preferencesState.syncBackupAutomatically = value
                if (value && backupSyncManager.needsAutomaticBackup()) {
                    backupSyncManager.makeAutomaticBackup()
                }
            },
            summaryProvider = {
                uiInfoService.resString(
                    R.string.settings_sync_backup_automatically_hint,
                    backupSyncManager.formatLastBackupTime(),
                )
          },
        )

        setupListPreference("mediaButtonBehaviour",
            mediaButtonService.mediaButtonBehavioursEntries(),
            onLoad = { preferencesState.mediaButtonBehaviour.id.toString() },
            onSave = { id: String ->
                preferencesState.mediaButtonBehaviour =
                    MediaButtonBehaviours.mustParseById(id.toLong())
            }
        )

        setupListPreference("customSongsOrdering",
            settingsEnumService.customSongsOrderingStringEntries(),
            onLoad = { preferencesState.customSongsOrdering.id.toString() },
            onSave = { id: String ->
                preferencesState.customSongsOrdering =
                    CustomSongsOrdering.mustParseById(id.toLong())
            }
        )

        setupListPreference("homeScreen",
            homeScreenEnumService.homeScreenEnumsEntries(),
            onLoad = { preferencesState.homeScreen.id.toString() },
            onSave = { id: String ->
                preferencesState.homeScreen = HomeScreenEnum.mustParseById(id.toLong())
            }
        )

        setupClickPreference("billingRemoveAds") {
            layoutController.showLayout(BillingLayoutController::class)
        }

        setupClickPreference("restoreCustomSongsBackup") {
            customSongsBackuper.showRestoreBackupDialog()
        }

        refreshFragment()
    }

    @Suppress("DEPRECATION")
    private fun refreshFragment() {
        fragmentManager?.let { fragmentManager ->
            val ft: FragmentTransaction = fragmentManager.beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }
            ft.detach(this).attach(this).commitAllowingStateLoss()
        }
    }

    private fun toggleAllMultiPreference(excludeLanguagesPreference: MultiSelectListPreference) {
        if (multiPreferenceAllSelected(excludeLanguagesPreference)) {
            excludeLanguagesPreference.values = emptySet()
        } else {
            excludeLanguagesPreference.values = excludeLanguagesPreference.entryValues
                .map { s -> s.toString() }.toSet()
        }
        excludeLanguagesPreference.callChangeListener(excludeLanguagesPreference.values)
    }

    private fun setupListPreference(
        key: String,
        entriesMap: LinkedHashMap<String, String>,
        onLoad: () -> String?,
        onSave: (id: String) -> Unit,
    ) {
        val preference = findPreference(key) as ListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                onSave(newValue.toString())
                true
            }
        preference.value = onLoad()
    }

    private fun setupMultiListPreference(
        key: String,
        entriesMap: LinkedHashMap<String, String>,
        onLoad: () -> Set<String>?,
        onSave: (ids: Set<String>) -> Unit,
        stringConverter: (ids: Set<String>, entriesMap: LinkedHashMap<String, String>) -> String
    ): MultiSelectListPreference {
        val preference = findPreference(key) as MultiSelectListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()

        RetryDelayed(5, 500, KotlinNullPointerException::class.java) {
            preference.values = onLoad()
        }

        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                if (newValue != null && newValue is Set<*>) {
                    @Suppress("unchecked_cast")
                    val newSet = newValue as Set<String>
                    onSave(newSet)
                    pref.summary = stringConverter(newSet, entriesMap)
                }
                true
            }
        preference.summary = stringConverter(preference.values, entriesMap)
        return preference
    }

    private fun multiPreferenceAllSelected(multiPreference: MultiSelectListPreference): Boolean {
        if (multiPreference.entryValues.size != multiPreference.values.size)
            return false
        val values = multiPreference.values
        multiPreference.entryValues.forEach { value ->
            if (value !in values)
                return false
        }
        return true
    }

    private fun setupSeekBarPreference(
        key: String,
        min: Number,
        max: Number,
        onLoad: () -> Float,
        onSave: (value: Float) -> Unit,
        stringConverter: (value: Float) -> String,
    ) {
        val preference = findPreference(key) as SeekBarPreference
        preference.isAdjustable = true
        preference.max = SEEKBAR_RESOLUTION
        val currentValueF: Float = onLoad()
        val minF = min.toFloat()
        val maxF = max.toFloat()
        preference.value = calculateProgress(minF, maxF, currentValueF)
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                val progress = newValue.toString().toFloat() / SEEKBAR_RESOLUTION
                val valueF = progress * (maxF - minF) + minF
                pref.summary = stringConverter(valueF)
                onSave(valueF)
                true
            }
        preference.summary = stringConverter(currentValueF)
    }

    private fun setupSwitchPreference(
        key: String,
        onLoad: () -> Boolean,
        onSave: (value: Boolean) -> Unit,
        summaryProvider: (() -> String)? = null,
    ) {
        val preference = findPreference(key) as SwitchPreference
        preference.isChecked = onLoad()
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                onSave(newValue as Boolean)
                summaryProvider?.let { provider ->
                    preference.summary = provider()
                }
                true
            }
        summaryProvider?.let { provider ->
            preference.summary = provider()
        }
    }

    private fun setupClickPreference(
        key: String,
        onClick: () -> Unit,
    ) {
        val button = findPreference(key)
        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            onClick.invoke()
            true
        }
    }

    private fun calculateProgress(min: Float, max: Float, value: Float): Int {
        val resolution = SEEKBAR_RESOLUTION
        if (value < min) {
            return 0
        }
        if (value > max) {
            return resolution
        }

        val progress = (value - min) / (max - min)
        return (progress * resolution).roundToInt()
    }

    private fun decimal3(value: Float): String {
        return decimalFormat3.format(value.toDouble())
    }

    private fun decimal1(value: Float): String {
        return decimalFormat1.format(value.toDouble())
    }

}