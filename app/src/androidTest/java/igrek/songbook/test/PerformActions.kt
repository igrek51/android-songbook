package igrek.songbook.test

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun waitFor(millis: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "Wait for $millis milliseconds."
        }

        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }
}

fun swipeUpABit(): ViewAction? {
    return actionWithAssertions(
            GeneralSwipeAction(
                    Swipe.SLOW,
                    translate(GeneralLocation.BOTTOM_CENTER, 0f, -0.083f * 0.1f),
                    GeneralLocation.CENTER,
                    Press.FINGER))
}

private fun translate(
        coords: CoordinatesProvider,
        dx: Float,
        dy: Float,
): CoordinatesProvider? {
    return MyTranslatedCoordinatesProvider(coords, dx, dy)
}

internal class MyTranslatedCoordinatesProvider(
        private val coordinatesProvider: CoordinatesProvider,
        private val dx: Float,
        private val dy: Float) : CoordinatesProvider {
    override fun calculateCoordinates(view: View): FloatArray {
        val xy = coordinatesProvider.calculateCoordinates(view)
        xy[0] += dx * view.width
        xy[1] += dy * view.height
        return xy
    }
}

fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
        var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View?): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}