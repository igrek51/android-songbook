@file:Suppress("DEPRECATION")

package igrek.songbook.screenshot

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.R
import igrek.songbook.activity.MainActivity
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.enums.ChordsInstrument
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.test.ScreenshotCapture
import igrek.songbook.test.swipeUpABit
import igrek.songbook.test.waitFor
import igrek.songbook.test.waitForVisibleView
import igrek.songbook.test.withIndex
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MobilePlScreenshotMaker {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_01_open_bright_song() {
        // Nie Płacz Ewka + birght theme + chords inline
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsAbove
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Nie Płacz Ewka"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Nie Płacz Ewka - Perfect")).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("01")
    }

    @Test
    fun test_02_transpose() {
        // Nim Wstanie Dzień + tranpose + chords right
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsAlignedRight
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Nim Wstanie Dzień"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Nim Wstanie Dzień - Edmund Fetting")).perform(click())

        onView(withId(R.id.transposeButton)).perform(click())
        onView(withId(R.id.transpose0Button)).perform(click())
        onView(withId(R.id.transposeM5Button)).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("02")
    }

    @Test
    fun test_03_autoscroll() {
        // Hey There Delilah + autoscroll + chords inline
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Hey There Delilah"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Hey There Delilah - Plain White T's")).perform(click())

        onView(withId(R.id.autoscrollButton)).perform(click())
        onView(withId(R.id.autoscrollToggleButton)).perform(click())
        onView(withId(R.id.autoscrollButton)).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("03")
    }

    @Test
    fun test_04_editor() {
        // Autobiografia Perfect in Editor
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Autobiografia Perfect"))
        onView(isRoot()).perform(waitFor(500))
        // choose more options
        onView(withIndex(withId(R.id.itemSongMoreButton), 0)).perform(click())
        // make editable copy
        onView(withText(R.string.action_song_copy)).perform(click())
        // Edit
        onView(isRoot()).perform(waitFor(200))
        onView(withText(R.string.song_copied_edit_it)).perform(click())
        // Open editor
        onView(withId(R.id.songContentEdit)).check(matches(isDisplayed()))
        onView(withId(R.id.songContentEdit)).perform(scrollTo(), click())
        onView(isRoot()).perform(waitFor(100))
        onView(withText(R.string.edit_song_detect_chords)).perform(scrollTo())
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("04")
    }

    @Test
    fun test_05_ukulele_diagram() {
        // Knocking on Heaven's Door + bright + Ukulele diagram
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 17.6f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.chordsInstrument = ChordsInstrument.UKULELE
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Knockin Heaven Door"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Knockin’ on Heaven’s Door - Bob Dylan")).perform(click())

        onView(withId(R.id.chordsHelpButton)).perform(click())
        onView(withText("G")).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("05")
    }

    @Test
    fun test_06_preview_chords_above() {
        // Hotel California + dark + chords above
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.DARK
        preferencesState.fontsize = 16.7f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsAbove
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Hotel California"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Hotel California - Eagles")).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("06")
    }

    @Test
    fun test_07_piano_diagram() {
        // Knocking on Heaven's Door + bright + Ukulele diagram
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 17.6f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.chordsInstrument = ChordsInstrument.PIANO
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Dust in the wind"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Dust In The Wind - Kansas")).perform(click())

        onView(withId(R.id.chordsHelpButton)).perform(click())
        onView(withText("D/F#")).perform(scrollTo(), click())

        onView(withText("ZNAJDŹ")).perform(click())
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Dsus4"))
        onView(withText("ZNAJDŹ")).perform(click())

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("07")
    }

    @Test
    fun test_08_super_cow() {
        // Top songs + Super Cow
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.DARK
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open about
        onView(withId(R.id.nav_view)).perform(swipeUp())
        onView(isRoot()).perform(waitFor(300))
        onView(isRoot()).perform(waitForVisibleView(R.id.nav_about, 5000))
        onView(withIndex(withId(R.id.nav_about), 0)).check(matches(isDisplayed()))
        onView(withIndex(withId(R.id.nav_about), 0)).perform(click())
        onView(withText(R.string.nav_about)).check(matches(isDisplayed()))
        // click ???
        onView(withId(android.R.id.button3)).check(matches(withText(R.string.action_secret)))
        onView(withId(android.R.id.button3)).perform(click())
        // type secret key
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("moooooo"))
        // click ok
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.action_check_secret)))
        onView(withId(android.R.id.button1)).perform(click())

        onView(withText(Matchers.containsString("Moooo"))).check(matches(isDisplayed()))
        onView(withText(Matchers.containsString("Secret Cow Level"))).check(matches(isDisplayed()))

        ScreenshotCapture.takeScreenshot("08")
        onView(isRoot()).perform(waitFor(500))
    }

    @Test
    fun test_09_settings() {
        // Settings
        val preferencesState = appFactory.settingsState.get()

        preferencesState.appLanguage = AppLanguage.POLISH // needs restart
        preferencesState.colorScheme = ColorScheme.DARK
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.GERMAN
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(isRoot()).perform(waitForVisibleView(R.id.navMenuButton, 5000))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_settings))
        onView(isRoot()).perform(waitFor(500))

        onView(withId(R.id.fragment_content)).perform(swipeUpABit(0.37f))
        onView(isRoot()).perform(waitFor(500))

        ScreenshotCapture.takeScreenshot("09")
    }

}
