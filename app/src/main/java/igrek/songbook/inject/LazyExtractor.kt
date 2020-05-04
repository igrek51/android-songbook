package igrek.songbook.inject

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LazyExtractor<F : Any, O : Any>(
        private var lazyInject: LazyInject<F>
) : ReadWriteProperty<O, F> {

    override fun getValue(thisRef: O, property: KProperty<*>): F {
        return lazyInject.get()
    }

    override fun setValue(thisRef: O, property: KProperty<*>, value: F) {
        lazyInject = SingletonInject { value }
    }
}
