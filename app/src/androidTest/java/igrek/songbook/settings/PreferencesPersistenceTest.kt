package igrek.songbook.settings

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.dagger.DaggerBreach
import igrek.songbook.info.logger.LoggerFactory
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

    private val logger = LoggerFactory.logger

    @Test
    fun test_default_values() {
        val preferencesService = DaggerBreach.factory().aPreferencesService()

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
        val preferencesService: PreferencesService = DaggerBreach.inject("aPreferencesServiceProvider")
        val preferencesState: PreferencesState = DaggerBreach.inject("aPreferencesStateProvider")

        preferencesService.clear()

        var pause: Long

        pause = preferencesService.getValue(PreferencesField.AutoscrollInitialPause)
        assertEquals(pause, 36000L)
        assertEquals(preferencesState.autoscrollInitialPause, 36000L)
        // check modified
        preferencesState.autoscrollInitialPause = 23
        pause = preferencesService.getValue(PreferencesField.AutoscrollInitialPause)
        assertEquals(pause, 23L)
        assertEquals(preferencesState.autoscrollInitialPause, 23L)

        preferencesService.saveAll()
        preferencesService.loadAll()
        // check if persisted after save & reload
        pause = preferencesService.getValue(PreferencesField.AutoscrollInitialPause)
        assertEquals(pause, 23L)
        assertEquals(preferencesState.autoscrollInitialPause, 23L)

        preferencesState.autoscrollInitialPause = 51
        assertEquals(preferencesState.autoscrollInitialPause, 51L)
        // lose changes by reloading without saving
        preferencesState.reload()
        assertEquals(preferencesState.autoscrollInitialPause, 23L)
        pause = preferencesService.getValue(PreferencesField.AutoscrollInitialPause)
        assertEquals(pause, 23L)
    }

    @Test
    fun test_mislead_types() {
        val preferencesService = DaggerBreach.factory().aPreferencesService()
        preferencesService.clear()

        try {
            var pause: Boolean = preferencesService.getValue(PreferencesField.AutoscrollInitialPause)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

        try {
            preferencesService.setValue(PreferencesField.AutoscrollInitialPause, true)
            check(false) { "should throw type error" }
        } catch (e: RuntimeException) {
        }

    }
}
