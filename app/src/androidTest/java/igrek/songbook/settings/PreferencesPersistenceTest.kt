package igrek.songbook.settings

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.SettingField
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

        preferencesService.clear()

        val restoreTransposition: Boolean = preferencesService.getValue(SettingField.RestoreTransposition)
        assertEquals(restoreTransposition, true)

        val chordsEndOfLine: Boolean = preferencesService.getValue(SettingField.ChordsEndOfLine)
        assertEquals(chordsEndOfLine, false)

        val chordsNotation: ChordsNotation = preferencesService.getValue(SettingField.ChordsNotationId)
        assertEquals(chordsNotation, ChordsNotation.default)
    }

    @Test
    fun test_saving_reading() {
        val preferencesService: PreferencesService = appFactory.preferencesService.get()
        val settingsState: SettingsState = appFactory.settingsState.get()

        preferencesService.clear()

        var current: Float = preferencesService.getValue(SettingField.AutoscrollSpeed)
        assertEquals(current, 0.200f)
        assertEquals(settingsState.autoscrollSpeed, 0.200f)
        // check modified
        settingsState.autoscrollSpeed = 23f
        current = preferencesService.getValue(SettingField.AutoscrollSpeed)
        assertEquals(current, 23f)
        assertEquals(settingsState.autoscrollSpeed, 23f)

        preferencesService.dumpAll()
        preferencesService.loadAll()
        // check if persisted after save & reload
        current = preferencesService.getValue(SettingField.AutoscrollSpeed)
        assertEquals(current, 23f)
        assertEquals(settingsState.autoscrollSpeed, 23f)

        settingsState.autoscrollSpeed = 51f
        assertEquals(settingsState.autoscrollSpeed, 51f)
        // lose changes by reloading without saving
        preferencesService.loadAll()
        assertEquals(settingsState.autoscrollSpeed, 23f)
        current = preferencesService.getValue(SettingField.AutoscrollSpeed)
        assertEquals(current, 23f)
    }

    @Test
    fun test_mislead_types() {
        val preferencesService = appFactory.preferencesService.get()
        preferencesService.clear()

        try {
            var pause: Boolean = preferencesService.getValue(SettingField.AutoscrollSpeed)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

        try {
            preferencesService.setValue(SettingField.AutoscrollSpeed, true)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

    }
}
