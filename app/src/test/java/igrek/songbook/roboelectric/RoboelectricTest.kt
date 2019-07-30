package igrek.songbook.roboelectric

import assertk.assertThat
import assertk.assertions.isNotNull
import igrek.songbook.activity.MainActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class RoboelectricTest {

    @Test
    fun testActivityCreation() {
        val activity = Robolectric.setupActivity(MainActivity::class.java)

        assertThat(activity).isNotNull()

        println("created activity: $activity")
    }

}
