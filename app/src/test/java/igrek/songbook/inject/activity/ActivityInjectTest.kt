package igrek.songbook.inject.activity

import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.PrototypeInject
import igrek.songbook.inject.SingletonInject
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ActivityInjectTest {
    @Test
    fun creatingServiceWithActivity() {
        val service1 = ServiceRequiringActivity()
        assertThat(service1.doSomethingWithActivity()).isTrue
    }

    @Test
    fun injectingSingletonCounter() {
        assertThat(Service2().singletonCounter.c++).isEqualTo(0)
        assertThat(Service2().singletonCounter.c++).isEqualTo(1)
    }

    @Test
    fun injectingPrototypeCounter() {
        assertThat(Service2().prototypeCounter.c++).isEqualTo(0)
        assertThat(Service2().prototypeCounter.c++).isEqualTo(0)
    }
}

class ActivityMock

private var appFactory: AppFactory = AppFactory(ActivityMock())

private class AppFactory(
        activity: ActivityMock,
) {
    val activity: LazyInject<ActivityMock> = SingletonInject { activity }

    val logger: LazyInject<Logger> = PrototypeInject { LoggerFactory.logger }

    val singletonCounter = SingletonInject { Counter() }
    val prototypeCounter = PrototypeInject { Counter() }
}

data class Counter(var c: Int = 0)

class ServiceRequiringActivity(
        activity: LazyInject<ActivityMock> = appFactory.activity,
        logger: LazyInject<Logger> = appFactory.logger,
) {
    private val activity by LazyExtractor(activity)
    private val logger by LazyExtractor(logger)

    fun doSomethingWithActivity(): Boolean {
        return activity is ActivityMock
    }
}

class Service2(
        appCompatActivity: LazyInject<ActivityMock> = appFactory.activity,
        logger: LazyInject<Logger> = appFactory.logger,
        singletonCounter: LazyInject<Counter> = appFactory.singletonCounter,
        prototypeCounter: LazyInject<Counter> = appFactory.prototypeCounter,
) {
    private val activity by LazyExtractor(appCompatActivity)

    val singletonCounter by LazyExtractor(singletonCounter)
    val prototypeCounter by LazyExtractor(prototypeCounter)

    private val privateCounter by LazyExtractor(prototypeCounter)
}
