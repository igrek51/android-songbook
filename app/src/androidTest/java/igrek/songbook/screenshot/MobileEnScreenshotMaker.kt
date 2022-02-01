package igrek.songbook.screenshot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.R
import igrek.songbook.activity.MainActivity
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.test.ScreenshotCapture
import igrek.songbook.test.swipeUpABit
import igrek.songbook.test.waitFor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MobileEnScreenshotMaker {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_01_open_bright_song() {
        // Zombie + birght theme + chords inline
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline

        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Zombie Cranberries"))
        onView(isRoot()).perform(waitFor(1000))
        // choose song
        onView(withText("Zombie - The Cranberries")).perform(click())

        onView(withId(R.id.songPreviewContainer)).perform(swipeUpABit())

        onView(isRoot()).perform(waitFor(1000))

        ScreenshotCapture.takeScreenshot("01")
    }

    @Test
    fun test_02_transpose() {
        // Soldier of Fortune + tranpose + chords right
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("02")
    }

    @Test
    fun test_03_autoscroll() {
        // Hey There Delilah + autoscroll + chords inline
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("03")
    }

    @Test
    fun test_04_editor() {
        // Space Oddity in Editor
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("04")
    }

    @Test
    fun test_05_ukulele_diagram() {
        // Knocking on Heaven's Door + Ukulele diagram
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("05")
    }

    @Test
    fun test_06_preview_chords_above() {
        // Eye in the sky + bright + chords above
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("06")
    }

    @Test
    fun test_07_settings() {
        // Settings
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("07")
    }

    @Test
    fun test_08_preview_dark() {
        // Shallow  + dark + chords inline
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline


        ScreenshotCapture.takeScreenshot("08")
    }

}
