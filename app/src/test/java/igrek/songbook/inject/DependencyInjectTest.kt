package igrek.songbook.inject

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class CrossDependencyInjectionTest {
    @Test
    fun test_crossDependcy() {
        val serviceA = AppContext.serviceA.get()
        assertThat(serviceA.show1()).isEqualTo("show3")
        val serviceA2 = ServiceA(serviceB = LazyInject { ServiceB() })
        assertThat(serviceA2.show1()).isEqualTo("show3")
    }
}

object AppContext {
    val serviceA = LazyInject { ServiceA() }
    val serviceB = LazyInject { ServiceB() }
}

class LazyInject<T : Any>(private val supplier: () -> T) {
    private var cached: T? = null

    fun get(): T {
        val cachedSnapshot = cached
        if (cachedSnapshot == null) {
            val notNull = supplier.invoke()
            cached = notNull
            return notNull
        }
        return cachedSnapshot
    }
}

class ServiceA(
        private val serviceB: LazyInject<ServiceB> = AppContext.serviceB,
) {
    fun show1(): String {
        return serviceB.get().show2()
    }

    fun show3(): String {
        return "show3"
    }
}

class ServiceB(
        private val serviceA: LazyInject<ServiceA> = AppContext.serviceA,
) {
    fun show2(): String {
        return serviceA.get().show3()
    }
}
