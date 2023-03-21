package igrek.songbook.settings

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesField
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
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

        val restoreTransposition: Boolean = preferencesService.getValue(PreferencesField.RestoreTransposition)
        assertEquals(restoreTransposition, true)

        val chordsEndOfLine: Boolean = preferencesService.getValue(PreferencesField.ChordsEndOfLine)
        assertEquals(chordsEndOfLine, false)

        val chordsNotation: ChordsNotation = preferencesService.getValue(PreferencesField.ChordsNotationId)
        assertEquals(chordsNotation, ChordsNotation.default)
    }

    @Test
    fun test_saving_reading() {
        val preferencesService: PreferencesService = appFactory.preferencesService.get()
        val preferencesState: PreferencesState = appFactory.preferencesState.get()

        preferencesService.clear()

        var current: Float = preferencesService.getValue(PreferencesField.AutoscrollSpeed)
        assertEquals(current, 0.200f)
        assertEquals(preferencesState.autoscrollSpeed, 0.200f)
        // check modified
        preferencesState.autoscrollSpeed = 23f
        current = preferencesService.getValue(PreferencesField.AutoscrollSpeed)
        assertEquals(current, 23f)
        assertEquals(preferencesState.autoscrollSpeed, 23f)

        preferencesService.saveAll()
        preferencesService.loadAll()
        // check if persisted after save & reload
        current = preferencesService.getValue(PreferencesField.AutoscrollSpeed)
        assertEquals(current, 23f)
        assertEquals(preferencesState.autoscrollSpeed, 23f)

        preferencesState.autoscrollSpeed = 51f
        assertEquals(preferencesState.autoscrollSpeed, 51f)
        // lose changes by reloading without saving
        preferencesService.loadAll()
        assertEquals(preferencesState.autoscrollSpeed, 23f)
        current = preferencesService.getValue(PreferencesField.AutoscrollSpeed)
        assertEquals(current, 23f)
    }

    @Test
    fun test_mislead_types() {
        val preferencesService = appFactory.preferencesService.get()
        preferencesService.clear()

        try {
            var pause: Boolean = preferencesService.getValue(PreferencesField.AutoscrollSpeed)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

        try {
            preferencesService.setValue(PreferencesField.AutoscrollSpeed, true)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

    }
}
