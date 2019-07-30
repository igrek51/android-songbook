package igrek.songbook.wip

import android.app.Activity
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import igrek.songbook.activity.MainActivity
import igrek.songbook.dagger.DaggerAndroidTestComponent
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.dagger.FactoryModule
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

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

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var songsRepository: SongsRepository

    @Before
    fun setUpDagger() {
        val activity = rule.activity
        val component = DaggerAndroidTestComponent.builder()
                .factoryModule(FactoryModule(activity))
                .build()
        DaggerIoc.factoryComponent = component
        component.inject(this)
        logger.warn("====== Running Android Instrumentation Test: WIPFeatureTest ======")
    }

    @Test
    @Ignore
    fun factoryReset() {
        preferencesService.clear()
        songsRepository.factoryReset()
    }


    @Test
    //	@Ignore
    fun testWipFeature() {

    }

}
