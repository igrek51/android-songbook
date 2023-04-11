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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
@OptIn(DelicateCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FeatureInProgressTest {

    private val logger = LoggerFactory.logger

    @Rule
    @JvmField
    var rule = ActivityTestRule(MainActivity::class.java)

    private lateinit var activity: Activity
    private lateinit var preferencesService: PreferencesService
    private lateinit var songsRepository: SongsRepository

    @Before
    fun setUpDependencies() {
//        val ruleActivity = rule.activity
//        AppContextFactory.createAppContext(ruleActivity)
//
//        activity = appFactory.activity.get()
//        preferencesService = appFactory.preferencesService.get()
//        songsRepository = appFactory.songsRepository.get()

        logger.warn("====== Running Android Instrumentation Test: FeatureInProgressTest ======")
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
        GlobalScope.launch {
            delay(1000)

            val userDataDao = appFactory.userDataDao.get()
            repeat(1000) {
                logger.warn("Test run: $it")
                userDataDao.load()
            }
        }
    }

}
