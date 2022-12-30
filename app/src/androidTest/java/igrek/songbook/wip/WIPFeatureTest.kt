package igrek.songbook.wip

import android.app.Activity
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.AppContextFactory
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 * @see [Testing documentation](http://d.android.com/tools/testing)
 * A test for a Work-in-progress-features
 */
@RunWith(AndroidJUnit4::class)
class WIPFeatureTest {

    private val logger = LoggerFactory.logger

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    lateinit var activity: Activity
    lateinit var preferencesService: PreferencesService
    lateinit var songsRepository: SongsRepository

    @Before
    fun setUpDependencies() {
        val ruleActivity = rule.activity
        AppContextFactory.createAppContext(ruleActivity)

        activity = appFactory.activity.get()
        preferencesService = appFactory.preferencesService.get()
        songsRepository = appFactory.songsRepository.get()

        logger.warn("====== Running Android Instrumentation Test: WIPFeatureTest ======")
    }

    @Test
    @Ignore("Development purposes")
    fun factoryReset() {
        preferencesService.clear()
        songsRepository.fullFactoryReset()
    }


    @Test
    //	@Ignore
    fun testWipFeature() {

    }

}
