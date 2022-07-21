package igrek.songbook.inject

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CrossDependencyInjectionTest {
    @Test
    fun test_circularDependcyResolving() {
        val serviceA = appFactory2.serviceA.get()
        assertThat(serviceA.show1()).isEqualTo("show3")
        val serviceA2 = ServiceA(serviceB = SingletonInject { ServiceB() })
        assertThat(serviceA2.show1()).isEqualTo("show3")
    }

    @Test
    fun creatingManyFactoryInstances() {
        assertThat(appFactory2.serviceA.get().show4()).isEqualTo("")

        TestAppContextFactory.createApp("ctx")
        assertThat(appFactory2.serviceA.get().show4()).isEqualTo("ctx")

        TestAppContextFactory.createApp("brand-new")
        assertThat(appFactory2.serviceA.get().show4()).isEqualTo("brand-new")
    }
}

private var appFactory2: AppFactory2 = AppFactory2("")

private object TestAppContextFactory {
    fun createApp(parameterP: String) {
        appFactory2 = AppFactory2(parameterP)
    }
}

private class AppFactory2(
        parameterP: String
) {
    val parameterP = SingletonInject { parameterP }
    val serviceA = SingletonInject { ServiceA() }
    val serviceB = SingletonInject { ServiceB() }
}

class ServiceA(
        serviceB: LazyInject<ServiceB> = appFactory2.serviceB,
        parameterP: LazyInject<String> = appFactory2.parameterP,
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
        serviceA: LazyInject<ServiceA> = appFactory2.serviceA,
) {
    private val serviceA: ServiceA by LazyExtractor(serviceA)

    fun show2(): String {
        return serviceA.show3()
    }
}
