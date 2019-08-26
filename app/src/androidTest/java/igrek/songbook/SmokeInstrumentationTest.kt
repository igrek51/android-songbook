package igrek.songbook

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class SmokeInstrumentationTest {

    @Test
    fun test_package_name() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertThat(appContext.packageName).isEqualTo("igrek.songbook")
    }
}
