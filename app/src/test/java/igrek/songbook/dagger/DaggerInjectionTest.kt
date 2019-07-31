package igrek.songbook.dagger

import android.app.Activity
import assertk.assertThat
import assertk.assertions.isNotNull
import igrek.songbook.activity.MainActivity
import igrek.songbook.dagger.base.DaggerTestComponent
import igrek.songbook.dagger.base.TestModule
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import javax.inject.Inject

class DaggerInjectionTest {

    @Inject
    lateinit var activity: Activity

    @Before
    fun setUp() {
        val activity = Mockito.mock(MainActivity::class.java)

        val component = DaggerTestComponent.builder()
                .factoryModule(TestModule(activity))
                .build()

        DaggerIoc.factoryComponent = component

        component.inject(this)
    }

    @Test
    fun testActivityInjection() {
        assertThat(activity).isNotNull()
        println("injected activity: $activity")
    }

}
