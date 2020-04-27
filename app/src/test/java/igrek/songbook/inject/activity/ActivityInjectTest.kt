package igrek.songbook.inject.activity

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
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

private var appFactory: AppFactory = AppFactory(AppCompatActivity())

private object AppContextFactory {
    fun createAppContext(activity: AppCompatActivity) {
        appFactory = AppFactory(activity)
    }
}

private class AppFactory(
        activity: AppCompatActivity,
) {
    val activity: LazyInject<Activity> = SingletonInject { activity }
    val appCompatActivity: LazyInject<AppCompatActivity> = SingletonInject { activity }

    val context: LazyInject<Context> = SingletonInject { activity.applicationContext }
    val logger: LazyInject<Logger> = PrototypeInject { LoggerFactory.logger }

    val singletonCounter = PrototypeInject { Counter() }
    val prototypeCounter = PrototypeInject { Counter() }
}

data class Counter(var c: Int = 0)

class ServiceRequiringActivity(
        activity: LazyInject<Activity> = appFactory.activity,
        logger: LazyInject<Logger> = appFactory.logger,
) {
    private val activity: Activity by LazyExtractor(activity)
    private val logger: Logger by LazyExtractor(logger)

    fun doSomethingWithActivity(): Boolean {
        return !activity.isChild
    }
}

class Service2(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        logger: LazyInject<Logger> = appFactory.logger,
        singletonCounter: LazyInject<Counter> = appFactory.singletonCounter,
        prototypeCounter: LazyInject<Counter> = appFactory.prototypeCounter,
) {
    private val activity by LazyExtractor(appCompatActivity)

    val singletonCounter by LazyExtractor(singletonCounter)
    val prototypeCounter by LazyExtractor(prototypeCounter)
}
