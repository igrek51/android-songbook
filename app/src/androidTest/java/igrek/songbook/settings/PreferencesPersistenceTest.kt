package igrek.songbook.settings

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.FloatField
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.SettingsState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesPersistenceTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_default_values() {
        val preferencesService = appFactory.preferencesService.get()
        val settingsState = appFactory.settingsState.get()

        preferencesService.clear()

        val restoreTransposition: Boolean = settingsState.restoreTransposition
        assertEquals(restoreTransposition, true)

        val randomPlaylistSongs: Boolean = settingsState.randomPlaylistSongs
        assertEquals(randomPlaylistSongs, false)

        val chordsNotation: ChordsNotation = settingsState.chordsNotation
        assertEquals(chordsNotation, ChordsNotation.default)
    }

    @Test
    fun test_saving_reading() {
        val preferencesService: PreferencesService = appFactory.preferencesService.get()
        val settingsState: SettingsState = appFactory.settingsState.get()

        preferencesService.clear()

        var current: Float = settingsState.autoscrollSpeed
        assertEquals(current, 0.200f)
        assertEquals(settingsState.autoscrollSpeed, 0.200f)
        // check modified
        settingsState.autoscrollSpeed = 23f
        current = preferencesService.getValue(FloatField("autoscrollSpeed", 0.200f))
        assertEquals(current, 23f)
        assertEquals(settingsState.autoscrollSpeed, 23f)

        preferencesService.dumpAll()
        preferencesService.reload()
        // check if persisted after save & reload
        current = preferencesService.getValue(FloatField("autoscrollSpeed", 0.200f))
        assertEquals(current, 23f)
        assertEquals(settingsState.autoscrollSpeed, 23f)

        settingsState.autoscrollSpeed = 51f
        assertEquals(settingsState.autoscrollSpeed, 51f)
        // lose changes by reloading without saving
        preferencesService.reload()
        assertEquals(settingsState.autoscrollSpeed, 23f)
        current = preferencesService.getValue(FloatField("autoscrollSpeed", 0.200f))
        assertEquals(current, 23f)
    }

    @Test
    fun test_mislead_types() {
        val preferencesService = appFactory.preferencesService.get()
        preferencesService.clear()

        try {
            preferencesService.setValue(FloatField("autoscrollSpeed", 0.200f), true)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

    }
}
