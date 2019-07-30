package igrek.songbook.settings

import android.os.Bundle
import android.support.v14.preference.MultiSelectListPreference
import android.support.v14.preference.SwitchPreference
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.SeekBarPreference
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.LayoutController
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesUpdater
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var uiInfoService: dagger.Lazy<UiInfoService>
    @Inject
    lateinit var uiResourceService: dagger.Lazy<UiResourceService>
    @Inject
    lateinit var activity: dagger.Lazy<AppCompatActivity>
    @Inject
    lateinit var lyricsThemeService: dagger.Lazy<LyricsThemeService>
    @Inject
    lateinit var appLanguageService: dagger.Lazy<AppLanguageService>
    @Inject
    lateinit var chordsNotationService: dagger.Lazy<ChordsNotationService>
    @Inject
    lateinit var preferencesUpdater: dagger.Lazy<PreferencesUpdater>

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
        DaggerIoc.factoryComponent.inject(this)
        setPreferencesFromResource(R.xml.settings_def, rootKey)

        setupListPreference("applicationLanguage",
                appLanguageService.get().languageEntries(),
                onLoad = { preferencesUpdater.get().appLanguage?.langCode },
                onSave = { id: String ->
                    preferencesUpdater.get().appLanguage = AppLanguage.parseByLangCode(id)
                }
        )

        setupListPreference("chordsNotation",
                chordsNotationService.get().chordsNotationEntries(),
                onLoad = { preferencesUpdater.get().chordsNotation?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.get().chordsNotation = ChordsNotation.parseById(id.toLong())
                }
        )

        setupListPreference("fontTypeface",
                lyricsThemeService.get().fontTypefaceEntries(),
                onLoad = { preferencesUpdater.get().fontTypeface?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.get().fontTypeface = FontTypeface.parseById(id)
                }
        )

        setupListPreference("colorScheme",
                lyricsThemeService.get().colorSchemeEntries(),
                onLoad = { preferencesUpdater.get().colorScheme?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.get().colorScheme = ColorScheme.parseById(id.toLong())
                }
        )

        // FIXME add hiding languages filter
//        val preference = setupMultiListPreference("excludedLanguagesFilter",
//                appLanguageService.languageFilterEntries(),
//                onLoad = {
//                    preferencesUpdater.excludedLanguages
//                            .map { language -> language.langCode }
//                            .toMutableSet()
//                },
//                onSave = { ids: Set<String> ->
//                    val langsStr = ids.joinToString(separator = ";")
//                    preferencesUpdater.excludedLanguages = appLanguageService.string2Languages(langsStr)
//                },
//                stringConverter = { ids: Set<String>, entriesMap: LinkedHashMap<String, String> ->
//                    if (ids.isEmpty())
//                        uiResourceService.resString(R.string.none)
//                    else
//                        ids.map { id -> entriesMap[id]!! }.sorted().joinToString(separator = ", ")
//                }
//        )

        setupSeekBarPreference("autoscrollInitialPause", min = 0, max = 90000,
                onLoad = { preferencesUpdater.get().autoscrollInitialPause.toFloat() },
                onSave = { value: Float ->
                    preferencesUpdater.get().autoscrollInitialPause = value.toLong()
                },
                stringConverter = { value: Float ->
                    uiResourceService.get().resString(R.string.settings_scroll_initial_pause_value, msToS(value).toString())
                }
        )

        setupSeekBarPreference("autoscrollSpeed", min = AutoscrollService.MIN_SPEED, max = AutoscrollService.MAX_SPEED,
                onLoad = { preferencesUpdater.get().autoscrollSpeed },
                onSave = { value: Float ->
                    preferencesUpdater.get().autoscrollSpeed = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.get().resString(R.string.settings_autoscroll_speed_value, decimal3(value))
                }
        )

        setupSeekBarPreference("fontSize", min = 5, max = 100,
                onLoad = { preferencesUpdater.get().fontsize },
                onSave = { value: Float ->
                    preferencesUpdater.get().fontsize = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.get().resString(R.string.settings_font_size_value, decimal1(value))
                }
        )

        setupSwitchPreference("autoscrollSpeedAutoAdjustment",
                onLoad = { preferencesUpdater.get().autoscrollSpeedAutoAdjustment },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().autoscrollSpeedAutoAdjustment = value
                }
        )

        setupSwitchPreference("autoscrollSpeedVolumeKeys",
                onLoad = { preferencesUpdater.get().autoscrollSpeedVolumeKeys },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().autoscrollSpeedVolumeKeys = value
                }
        )

        setupSwitchPreference("randomFavouriteSongsOnly",
                onLoad = { preferencesUpdater.get().randomFavouriteSongsOnly },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().randomFavouriteSongsOnly = value
                }
        )

        // not saving preferences here as they will be saved on activity stop
    }

    private fun setupListPreference(key: String,
                                    entriesMap: LinkedHashMap<String, String>,
                                    onLoad: () -> String?,
                                    onSave: (id: String) -> Unit) {
        val preference = findPreference(key) as ListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            onSave(newValue.toString())
            true
        }
        preference.value = onLoad()
    }

    private fun setupMultiListPreference(key: String,
                                         entriesMap: LinkedHashMap<String, String>,
                                         onLoad: () -> Set<String>?,
                                         onSave: (ids: Set<String>) -> Unit,
                                         stringConverter: (ids: Set<String>, entriesMap: LinkedHashMap<String, String>) -> String): MultiSelectListPreference {
        val preference = findPreference(key) as MultiSelectListPreference
        preference.entryValues = entriesMap.keys.toTypedArray()
        preference.entries = entriesMap.values.toTypedArray()
        preference.values = onLoad()
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            val newSet = newValue as Set<String>
            onSave(newSet)
            pref.summary = stringConverter(newSet, entriesMap)
            true
        }
        preference.summary = stringConverter(preference.values, entriesMap)
        return preference
    }

    private fun setupSeekBarPreference(key: String,
                                       min: Number,
                                       max: Number,
                                       onLoad: () -> Float,
                                       onSave: (value: Float) -> Unit,
                                       stringConverter: (value: Float) -> String) {
        val preference = findPreference(key) as SeekBarPreference
        preference.isAdjustable = true
        preference.max = SEEKBAR_RESOLUTION
        val currentValueF: Float = onLoad()
        val minF = min.toFloat()
        val maxF = max.toFloat()
        preference.value = calculateProgress(minF, maxF, currentValueF, SEEKBAR_RESOLUTION)
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            val progress = newValue.toString().toFloat() / SEEKBAR_RESOLUTION
            val valueF = progress * (maxF - minF) + minF
            pref.summary = stringConverter(valueF)
            onSave(valueF)
            true
        }
        preference.summary = stringConverter(currentValueF)
    }

    private fun setupSwitchPreference(key: String,
                                      onLoad: () -> Boolean,
                                      onSave: (value: Boolean) -> Unit) {
        val preference = findPreference(key) as SwitchPreference
        preference.isChecked = onLoad()
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            onSave(newValue as Boolean)
            true
        }
    }

    private fun calculateProgress(min: Float, max: Float, value: Float, resolution: Int): Int {
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