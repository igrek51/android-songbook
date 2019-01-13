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
import igrek.songbook.songpreview.theme.ColorScheme
import igrek.songbook.songpreview.theme.FontTypeface
import igrek.songbook.songpreview.theme.LyricsThemeService
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var lyricsThemeService: LyricsThemeService
    @Inject
    lateinit var appLanguageService: AppLanguageService
    @Inject
    lateinit var chordsNotationService: ChordsNotationService
    @Inject
    lateinit var preferencesUpdater: PreferencesUpdater

    private var decimalFormat4: DecimalFormat
    private var decimalFormat1: DecimalFormat

    companion object {
        const val SEEKBAR_RESOLUTION = 10000
    }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        decimalFormat4 = DecimalFormat("#.####")
        decimalFormat4.roundingMode = RoundingMode.HALF_UP
        decimalFormat1 = DecimalFormat("#.#")
        decimalFormat1.roundingMode = RoundingMode.HALF_UP
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_def, rootKey)

        setupListPreference("applicationLanguage",
                appLanguageService.languageEntries(),
                onLoad = { preferencesUpdater.appLanguage?.langCode },
                onSave = { id: String ->
                    preferencesUpdater.appLanguage = AppLanguage.parseByLangCode(id)
                }
        )

        setupListPreference("chordsNotation",
                chordsNotationService.chordsNotationEntries(),
                onLoad = { preferencesUpdater.chordsNotation?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.chordsNotation = ChordsNotation.parseById(id.toLong())
                }
        )

        setupListPreference("fontTypeface",
                lyricsThemeService.fontTypefaceEntries(),
                onLoad = { preferencesUpdater.fontTypeface?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.fontTypeface = FontTypeface.parseById(id)
                }
        )

        setupListPreference("colorScheme",
                lyricsThemeService.colorSchemeEntries(),
                onLoad = { preferencesUpdater.colorScheme?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.colorScheme = ColorScheme.parseById(id.toLong())
                }
        )

        setupMultiListPreference("excludedLanguagesFilter",
                appLanguageService.languageFilterEntries(),
                onLoad = {
                    preferencesUpdater.excludedLanguages
                            .map { language -> language.langCode }
                            .toMutableSet()
                },
                onSave = { ids: Set<String> ->
                    val langsStr = ids.joinToString(separator = ";")
                    preferencesUpdater.excludedLanguages = appLanguageService.string2Languages(langsStr)
                },
                stringConverter = { ids: Set<String>, entriesMap: LinkedHashMap<String, String> ->
                    if (ids.isEmpty())
                        uiResourceService.resString(R.string.none)
                    else
                        ids.map { id -> entriesMap[id]!! }.sorted().joinToString(separator = ", ")
                }
        )

        setupSeekBarPreference("autoscrollInitialPause", min = 0, max = 90000,
                onLoad = { preferencesUpdater.autoscrollInitialPause.toFloat() },
                onSave = { value: Float ->
                    preferencesUpdater.autoscrollInitialPause = value.toLong()
                },
                stringConverter = { value: Float ->
                    uiResourceService.resString(R.string.settings_scroll_initial_pause_value, msToS(value).toString())
                }
        )

        setupSeekBarPreference("autoscrollSpeed", min = 0, max = 1.0,
                onLoad = { preferencesUpdater.autoscrollSpeed },
                onSave = { value: Float ->
                    preferencesUpdater.autoscrollSpeed = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.resString(R.string.settings_autoscroll_speed_value, decimal4(value))
                }
        )

        setupSeekBarPreference("fontSize", min = 5, max = 100,
                onLoad = { preferencesUpdater.fontsize },
                onSave = { value: Float ->
                    preferencesUpdater.fontsize = value
                },
                stringConverter = { value: Float ->
                    uiResourceService.resString(R.string.settings_font_size_value, decimal1(value))
                }
        )

        setupSwitchPreference("autoscrollSpeedAutoAdjustment",
                onLoad = { preferencesUpdater.autoscrollSpeedAutoAdjustment },
                onSave = { value: Boolean ->
                    preferencesUpdater.autoscrollSpeedAutoAdjustment = value
                }
        )

        setupSwitchPreference("autoscrollSpeedDpadKeys",
                onLoad = { preferencesUpdater.autoscrollSpeedDpadKeys },
                onSave = { value: Boolean ->
                    preferencesUpdater.autoscrollSpeedDpadKeys = value
                }
        )

        setupSwitchPreference("autoscrollSpeedDpadKeys",
                onLoad = { preferencesUpdater.randomFavouriteSongsOnly },
                onSave = { value: Boolean ->
                    preferencesUpdater.randomFavouriteSongsOnly = value
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
                                         stringConverter: (ids: Set<String>, entriesMap: LinkedHashMap<String, String>) -> String) {
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
        preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
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

    private fun decimal4(value: Float): String {
        return decimalFormat4.format(value.toDouble())
    }

    private fun decimal1(value: Float): String {
        return decimalFormat1.format(value.toDouble())
    }

    private fun msToS(ms: Float): Long {
        return ((ms + 500) / 1000).toLong()
    }

}