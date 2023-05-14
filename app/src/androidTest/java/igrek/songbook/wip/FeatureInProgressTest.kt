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
import kotlinx.coroutines.*
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
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class FeatureInProgressTest {

    private val logger = LoggerFactory.logger

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUpDependencies() {
        logger.warn("====== Running Android Instrumentation Test: FeatureInProgressTest ======")
    }

    @Test
    @Ignore("Development purposes")
    fun factoryReset() {
        val ruleActivity = rule.activity
        AppContextFactory.createAppContext(ruleActivity)

        val activity: Activity = appFactory.activity.get()
        val preferencesService: PreferencesService = appFactory.preferencesService.get()
        val songsRepository: SongsRepository = appFactory.songsRepository.get()

        preferencesService.clear()
        songsRepository.fullFactoryReset()
    }


    @Test
    @Ignore("Development purposes")
    fun testWipFeature() {
        runBlocking {
            delay(1000)

            repeat(1000) {
                logger.info("Test run: $it")

                GlobalScope.launch {
                    val activity = appFactory.appCompatActivity.get()
                    AppContextFactory.createAppContext(activity)

                    val userDataDao = appFactory.userDataDao.get()
                    userDataDao.load()
                }.join()

            }
        }
    }

}
