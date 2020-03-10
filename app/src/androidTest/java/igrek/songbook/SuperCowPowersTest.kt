package igrek.songbook

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class SuperCowPowersTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun test_cowDialogShows() {
        onView(withId(R.id.nav_view)).check(matches(not(isDisplayed())))
        // open nav drawer
        onView(withId(R.id.navMenuButton)).perform(click())
        onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
        // open about
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_about))
        onView(withText(R.string.nav_about)).check(matches(isDisplayed()))
        // click ???
        onView(withId(android.R.id.button3)).check(matches(withText(R.string.action_secret)))
        onView(withId(android.R.id.button3)).perform(click())
        // type secret key
        onView(allOf(withClassName(endsWith("EditText")), withText(""))).perform(replaceText("moooooo"))
        // click ok
        onView(withId(android.R.id.button1)).check(matches(withText(R.string.action_check_secret)))
        onView(withId(android.R.id.button1)).perform(click())

        onView(withText(containsString("Moooo"))).check(matches(isDisplayed()))
        onView(withText(containsString("Super Cow Powers"))).check(matches(isDisplayed()))
    }
}
