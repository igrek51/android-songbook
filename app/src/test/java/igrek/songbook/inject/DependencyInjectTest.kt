package igrek.songbook.inject

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


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
        serviceB: LazyInject<ServiceB> = AppContext.serviceB,
) {
    private val serviceB: ServiceB by LazyExtractor(serviceB)

    fun show1(): String {
        return serviceB.show2()
    }

    fun show3(): String {
        return "show3"
    }
}

class ServiceB(
        serviceA: LazyInject<ServiceA> = AppContext.serviceA,
) {
    private val serviceA: ServiceA by LazyExtractor(serviceA)

    fun show2(): String {
        return serviceA.show3()
    }
}

class LazyExtractor<F : Any, O : Any>(
        private val lazyInject: LazyInject<F>
) : ReadOnlyProperty<O, F> {

    override fun getValue(thisRef: O, property: KProperty<*>): F {
        return lazyInject.get()
    }
}
