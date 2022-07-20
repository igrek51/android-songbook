package igrek.songbook.screenshot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
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
import igrek.songbook.test.swipeUpABit
import igrek.songbook.test.waitFor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.endsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowcaseVideoMaker {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_video_showcase() {
        /*
        Open drawer
        Search
        Find Wish pink
        swipe up and down
        tranpose +1 x5
        autoscroll on
        chords diagram, show Am
        Star a song
         */
        val preferencesState = appFactory.preferencesState.get()

        preferencesState.appLanguage = AppLanguage.ENGLISH // needs restart
        preferencesState.colorScheme = ColorScheme.BRIGHT
        preferencesState.fontsize = 27.2f
        preferencesState.chordsNotation = ChordsNotation.ENGLISH
        preferencesState.fontTypeface = FontTypeface.default
        preferencesState.chordsDisplayStyle = DisplayStyle.ChordsInline
        preferencesState.restoreTransposition = false
        preferencesState.autoscrollSpeed = 0.234f
        preferencesState.chordsInstrument = ChordsInstrument.GUITAR

        // open nav drawer
        onView(isRoot()).perform(waitFor(1500))
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        onView(isRoot()).perform(waitFor(1000))
        // open Search
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_search))
        onView(isRoot()).perform(waitFor(1000))
        // type song name
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(typeText("pink floyd"))
        onView(isRoot()).perform(waitFor(1000))
        onView(allOf(withClassName(endsWith("EditText")), withText("pink floyd"))).perform(typeText(" wish"))
        onView(isRoot()).perform(waitFor(1000))
        // choose song
        onView(withText("Wish You Were Here - Pink Floyd")).perform(click())
        onView(isRoot()).perform(waitFor(1000))

        // swipe up and down
        onView(withId(R.id.songPreviewContainer)).perform(swipeUpABit(0.75f, swiper = Swipe.FAST))
        onView(isRoot()).perform(waitFor(500))
        onView(withId(R.id.songPreviewContainer)).perform(swipeUpABit(-0.75f, swiper = Swipe.FAST))
        onView(isRoot()).perform(waitFor(1000))

        // transpose
        onView(withId(R.id.transposeButton)).perform(click())
        onView(isRoot()).perform(waitFor(1000))

        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(isRoot()).perform(waitFor(400))
        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(isRoot()).perform(waitFor(400))
        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(isRoot()).perform(waitFor(400))
        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(isRoot()).perform(waitFor(400))
        onView(withId(R.id.transposeP1Button)).perform(click())
        onView(isRoot()).perform(waitFor(400))

        onView(withId(R.id.transposeButton)).perform(click())
        onView(isRoot()).perform(waitFor(1000))

        // autoscroll on
        onView(withId(R.id.autoscrollButton)).perform(click())
        onView(isRoot()).perform(waitFor(1500))
        onView(withId(R.id.autoscrollToggleButton)).perform(click())
        onView(isRoot()).perform(waitFor(6000))

        // chords diagram
        onView(withId(R.id.chordsHelpButton)).perform(click())
        onView(isRoot()).perform(waitFor(1000))
        onView(withText("G")).perform(click())
        onView(isRoot()).perform(waitFor(2000))
        onView(withText(R.string.action_close)).perform(click())
        onView(isRoot()).perform(waitFor(500))

        // star a song
        onView(withId(R.id.setFavouriteButton)).perform(click())

        onView(isRoot()).perform(waitFor(10000))
    }

}
