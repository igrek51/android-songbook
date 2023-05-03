package igrek.songbook.test

import android.view.View
import androidx.core.view.isVisible
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.TimeoutException


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

fun swipeUpABit(factor: Float = 0.5f, swiper: Swiper = Swipe.SLOW): ViewAction? {
    return actionWithAssertions(
            GeneralSwipeAction(
                    swiper,
                    translate(GeneralLocation.CENTER, 0f, factor / 2),
                    translate(GeneralLocation.CENTER, 0f, -factor / 2),
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

fun waitForVisibleView(viewId: Int, timeout: Long): ViewAction {
    return object : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "wait for a specific view with id $viewId; during $timeout millis."
        }

        override fun perform(uiController: UiController, rootView: View) {
            uiController.loopMainThreadUntilIdle()
            val startTime = System.currentTimeMillis()
            val endTime = startTime + timeout
            val viewMatcher = withId(viewId)

            do {
                for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                    if (viewMatcher.matches(child)) {
                        if (child.isVisible)
                            return
                    }
                }
                uiController.loopMainThreadForAtLeast(100)
            } while (System.currentTimeMillis() < endTime)

            throw PerformException.Builder()
                .withCause(TimeoutException())
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(rootView))
                .build()
        }
    }
}
