package igrek.songbook.dagger


import igrek.songbook.dagger.base.BaseDaggerTest
import org.junit.Test

class BaseDaggerTestTest : BaseDaggerTest() {

    @Test
    fun testActivityInjection() {
        println("injected activity: $activity")
    }

    @Test
    fun testLoggerMock() {
        logger.info("Hello dupa")
    }

}
