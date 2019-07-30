package igrek.songbook.dagger.base

import android.app.Activity
import igrek.songbook.activity.MainActivity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.Logger
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
abstract class BaseDaggerTest {

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var logger: Logger

    @Before
    fun setUp() {
        val activity = Robolectric.setupActivity(MainActivity::class.java)

        val component = DaggerTestComponent.builder()
                .factoryModule(TestModule(activity))
                .build()

        DaggerIoc.factoryComponent = component

        injectThis(component)
    }

    private fun injectThis(component: TestComponent) {
        component.inject(this)
    }
}
