package igrek.songbook.dagger.base

import android.app.Activity
import igrek.songbook.activity.MainActivity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.Logger
import org.junit.Before
import org.mockito.Mockito
import javax.inject.Inject

abstract class BaseDaggerTest {

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var logger: Logger

    @Before
    fun setUp() {
        val activity = Mockito.mock(MainActivity::class.java)

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
