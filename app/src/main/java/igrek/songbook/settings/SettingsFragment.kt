package igrek.songbook.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.*
import igrek.songbook.R
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.chords.diagram.ChordDiagramStyle
import igrek.songbook.chords.diagram.ChordsDiagramsService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.settings.buttons.MediaButtonBehaviours
import igrek.songbook.settings.buttons.MediaButtonService
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.util.RetryDelayed
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class SettingsFragment(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
    chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
    chordsInstrumentService: LazyInject<ChordsInstrumentService> = appFactory.chordsInstrumentService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    googleSyncManager: LazyInject<GoogleSyncManager> = appFactory.googleSyncManager,
    chordsDiagramsService: LazyInject<ChordsDiagramsService> = appFactory.chordsDiagramsService,
    mediaButtonService: LazyInject<MediaButtonService> = appFactory.mediaButtonService,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
) : PreferenceFragmentCompat() {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val activity by LazyExtractor(appCompatActivity)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val chordsInstrumentService by LazyExtractor(chordsInstrumentService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val googleSyncManager by LazyExtractor(googleSyncManager)
    private val chordsDiagramsService by LazyExtractor(chordsDiagramsService)
    private val mediaButtonService by LazyExtractor(mediaButtonService)
    private val layoutController by LazyExtractor(layoutController)

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
                }
        )

        setupListPreference("chordsInstrument",
                chordsInstrumentService.instrumentEntries(),
                onLoad = { preferencesState.chordsInstrument.id.toString() },
                onSave = { id: String ->
                    preferencesState.chordsInstrument = ChordsInstrument.parseById(id.toLong())
                            ?: ChordsInstrument.default
                }
        )

        setupListPreference("chordDiagramStyle",
                chordsDiagramsService.chordDiagramStyleEntries(),
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

        setupSeekBarPreference("autoscrollSpeed", min = AutoscrollService.MIN_SPEED, max = AutoscrollService.MAX_SPEED,
                onLoad = { preferencesState.autoscrollSpeed },
                onSave = { value: Float ->
                    preferencesState.autoscrollSpeed = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.resString(R.string.settings_autoscroll_speed_value, decimal3(value))
                }
        )

        setupSeekBarPreference("fontSize", min = 5, max = 100,
                onLoad = { preferencesState.fontsize },
                onSave = { value: Float ->
                    preferencesState.fontsize = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.resString(R.string.settings_font_size_value, decimal1(value))
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
                        uiResourceService.resString(R.string.none)
                    else
                        ids.map { id -> entriesMap[id].orEmpty() }.sorted().joinToString(separator = ", ")
                }
        )

        setupSwitchPreference("customSongsGroupCategories",
                onLoad = { preferencesState.customSongsGroupCategories },
                onSave = { value: Boolean ->
                    preferencesState.customSongsGroupCategories = value
                }
        )

        setupSwitchPreference("restoreTransposition",
                onLoad = { preferencesState.restoreTransposition },
                onSave = { value: Boolean ->
                    preferencesState.restoreTransposition = value
                }
        )

        setupClickPreference("settingsSyncSave") {
            SafeExecutor {
                googleSyncManager.syncSave()
            }
        }

        setupClickPreference("settingsSyncRestore") {
            ConfirmDialogBuilder().confirmAction(R.string.settings_sync_restore_confirm) {
                SafeExecutor {
                    googleSyncManager.syncRestore()
                }
            }
        }

        setupClickPreference("settingsPrivacyPolicy") {
            openPrivacyPolicy()
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

        setupListPreference("mediaButtonBehaviour",
                mediaButtonService.mediaButtonBehavioursEntries(),
                onLoad = { preferencesState.mediaButtonBehaviour.id.toString() },
                onSave = { id: String ->
                    preferencesState.mediaButtonBehaviour = MediaButtonBehaviours.mustParseById(id.toLong())
                }
        )

        setupClickPreference("billingRemoveAds") {
            layoutController.showLayout(BillingLayoutController::class)
        }

        refreshFragment()
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://docs.google.com/document/d/e/2PACX-1vTRgTqRx6Cwbn_uuLXCuad9YEK3qY7XNxMkil26ZBV5XZ_qn6L-CaXu3M39k-Gc6OErnCmsrY8QPT8e/pub")
        val i = Intent(Intent.ACTION_VIEW, uri)
        activity.startActivity(i)
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
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
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

        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
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
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
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
    ) {
        val preference = findPreference(key) as SwitchPreference
        preference.isChecked = onLoad()
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            onSave(newValue as Boolean)
            true
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

    private fun msToS(ms: Float): Long {
        return ((ms + 500) / 1000).toLong()
    }

}