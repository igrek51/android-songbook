package igrek.songbook.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesUpdater
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.util.RetryDelayed
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
    lateinit var chordsInstrumentService: dagger.Lazy<ChordsInstrumentService>
    @Inject
    lateinit var preferencesUpdater: dagger.Lazy<PreferencesUpdater>
    @Inject
    lateinit var songsRepository: dagger.Lazy<SongsRepository>

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
        Handler(Looper.getMainLooper()).post {
            lateInit()
        }
    }

    private fun lateInit() {
        setupListPreference("applicationLanguage",
                appLanguageService.get().languageEntries(),
                onLoad = { preferencesUpdater.get().appLanguage?.langCode },
                onSave = { id: String ->
                    preferencesUpdater.get().appLanguage = AppLanguage.parseByLangCode(id)
                }
        )

        setupListPreference("chordsInstrument",
                chordsInstrumentService.get().instrumentEntries(),
                onLoad = { preferencesUpdater.get().chordsInstrument?.id.toString() },
                onSave = { id: String ->
                    preferencesUpdater.get().chordsInstrument = ChordsInstrument.parseById(id.toLong())
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

        setupSwitchPreference("chordsEndOfLine",
                onLoad = { preferencesUpdater.get().chordsEndOfLine },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().chordsEndOfLine = value
                }
        )

        setupSwitchPreference("chordsAbove",
                onLoad = { preferencesUpdater.get().chordsAbove },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().chordsAbove = value
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

        val excludeLanguagesPreference = setupMultiListPreference("excludeFilterLanguages",
                appLanguageService.get().languageFilterEntries(),
                onLoad = {
                    songsRepository.get().exclusionDao.exclusionDb.languages.toMutableSet()
                },
                onSave = { ids: Set<String> ->
                    songsRepository.get().exclusionDao.setExcludedLanguages(ids.toMutableList())
                },
                stringConverter = { ids: Set<String>, entriesMap: LinkedHashMap<String, String> ->
                    if (ids.isEmpty())
                        uiResourceService.get().resString(R.string.none)
                    else
                        ids.map { id -> entriesMap[id]!! }.sorted().joinToString(separator = ", ")
                }
        )

        val excludeArtistsPreference = setupMultiListPreference("excludeFilterArtists",
                songsRepository.get().exclusionDao.allArtistsFilterEntries,
                onLoad = {
                    songsRepository.get().exclusionDao.exclusionDb.artistIds
                            .map { id -> id.toString() }
                            .toMutableSet()
                },
                onSave = { ids: Set<String> ->
                    val longIds = ids.map { id -> id.toLong() }.toMutableList()
                    songsRepository.get().exclusionDao.setExcludedArtists(longIds)
                },
                stringConverter = { ids: Set<String>, entriesMap: LinkedHashMap<String, String> ->
                    if (ids.isEmpty())
                        uiResourceService.get().resString(R.string.none)
                    else
                        ids.map { id -> entriesMap[id]!! }.sorted().joinToString(separator = ", ")
                }
        )

        setupClickPreference("settingsToggleAllLanguages") {
            toggleAllMultiPreference(excludeLanguagesPreference)
        }

        setupClickPreference("settingsToggleAllArtists") {
            toggleAllMultiPreference(excludeArtistsPreference)
        }

        setupSwitchPreference("customSongsGroupCategories",
                onLoad = { preferencesUpdater.get().customSongsGroupCategories },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().customSongsGroupCategories = value
                }
        )

        setupSwitchPreference("restoreTransposition",
                onLoad = { preferencesUpdater.get().restoreTransposition },
                onSave = { value: Boolean ->
                    preferencesUpdater.get().restoreTransposition = value
                }
        )
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

    private fun setupClickPreference(key: String,
                                     onClick: () -> Unit) {
        val button = findPreference(key)
        button.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            onClick.invoke()
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