package igrek.songbook.inject

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CrossDependencyInjectionTest {
    @Test
    fun test_circularDependcyResolving() {
        val serviceA = appFactory.serviceA.get()
        assertThat(serviceA.show1()).isEqualTo("show3")
        val serviceA2 = ServiceA(serviceB = SingletonInject { ServiceB() })
        assertThat(serviceA2.show1()).isEqualTo("show3")
    }

    @Test
    fun creatingManyFactoryInstances() {
        assertThat(appFactory.serviceA.get().show4()).isEqualTo("")

        AppContextFactory.createApp("ctx")
        assertThat(appFactory.serviceA.get().show4()).isEqualTo("ctx")

        AppContextFactory.createApp("brand-new")
        assertThat(appFactory.serviceA.get().show4()).isEqualTo("brand-new")
    }
}

private var appFactory: AppFactory = AppFactory("")

private object AppContextFactory {
    fun createApp(parameterP: String) {
        appFactory = AppFactory(parameterP)
    }
}

private class AppFactory(
        parameterP: String
) {
    val parameterP = SingletonInject { parameterP }
    val serviceA = SingletonInject { ServiceA() }
    val serviceB = SingletonInject { ServiceB() }
}

class ServiceA(
        serviceB: LazyInject<ServiceB> = appFactory.serviceB,
        parameterP: LazyInject<String> = appFactory.parameterP,
) {
    private val serviceB: ServiceB by LazyExtractor(serviceB)
    private val parameterP by LazyExtractor(parameterP)

    fun show1(): String {
        return serviceB.show2()
    }

    fun show3(): String {
        return "show3"
    }

    fun show4(): String {
        return parameterP
    }
}

class ServiceB(
        serviceA: LazyInject<ServiceA> = appFactory.serviceA,
) {
    private val serviceA: ServiceA by LazyExtractor(serviceA)

    fun show2(): String {
        return serviceA.show3()
    }
}
