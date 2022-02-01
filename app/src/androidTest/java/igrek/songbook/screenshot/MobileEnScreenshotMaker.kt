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
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.test.ScreenshotCapture
import igrek.songbook.test.swipeUpABit
import igrek.songbook.test.waitFor
import igrek.songbook.test.withIndex
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
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Zombie Cranberries"))
        onView(isRoot()).perform(waitFor(500))
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
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsAlignedRight
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Soldier of Fortune"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Soldier of Fortune - Deep Purple")).perform(click())

        onView(withId(R.id.transposeButton)).perform(click())
        onView(withId(R.id.transpose0Button)).perform(click())
        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(withId(R.id.transposeP1Button)).perform(click())

        onView(isRoot()).perform(waitFor(100))

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
        preferencesState.restoreTransposition = false

        // open nav drawer
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
        // Space Oddity in Editor
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Space Oddity"))
        onView(isRoot()).perform(waitFor(500))
        // choose more options
        onView(withIndex(withId(R.id.itemSongMoreButton), 0)).perform(click())
        // make editable copy
        onView(withText(R.string.action_song_copy)).perform(click())
        // Edit
        onView(isRoot()).perform(waitFor(200))
        onView(withText(R.string.action_song_edit)).perform(click())
        // Open editor
        onView(withId(R.id.songContentEdit)).check(matches(isDisplayed()))
        onView(withId(R.id.songContentEdit)).perform(scrollTo(), click())
        onView(isRoot()).perform(waitFor(100))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))
        onView(withId(R.id.songContentEdit)).perform(pressKey(KeyEvent.KEYCODE_PAGE_UP))

        onView(isRoot()).perform(waitFor(100))

        ScreenshotCapture.takeScreenshot("04")
    }

    @Test
    fun test_05_ukulele_diagram() {
        // Knocking on Heaven's Door + bright + Ukulele diagram
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 17.6f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.chordsInstrument = ChordsInstrument.UKULELE
        preferencesState.restoreTransposition = false

        // open nav drawer
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
        // Eye in the sky + dark + chords above
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.DARK
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsAbove
        preferencesState.restoreTransposition = false

        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("Eye in the sky"))
        onView(isRoot()).perform(waitFor(500))
        // choose song
        onView(withText("Eye In The Sky - Alan Parsons Project")).perform(click())

        onView(isRoot()).perform(waitFor(100))

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
        preferencesState.restoreTransposition = false


        ScreenshotCapture.takeScreenshot("07")
    }

    @Test
    fun test_08_super_cow() {
        // Wish You Were Here + dark + Super Cow
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.DARK
        preferencesState.fontsize = 20f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false


        ScreenshotCapture.takeScreenshot("08")
    }

}
