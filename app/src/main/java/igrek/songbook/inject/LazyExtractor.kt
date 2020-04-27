package igrek.songbook.inject

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class LazyExtractor<F : Any, O : Any>(
        private val lazyInject: LazyInject<F>
) : ReadOnlyProperty<O, F> {

    override fun getValue(thisRef: O, property: KProperty<*>): F {
        return lazyInject.get()
    }
}
