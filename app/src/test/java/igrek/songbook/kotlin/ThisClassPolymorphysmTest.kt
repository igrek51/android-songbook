package igrek.songbook.kotlin

import org.junit.Test
import kotlin.reflect.KClass

class ThisClassPolymorphysmTest {

    open class Parent {
        fun clazz(): KClass<out Parent> {
            return this::class
        }
    }

    open class Child : Parent()

    @Test
    fun test_this_class_polymorphysm() {
        val parent = Parent()
        val child = Child()

        assert(Parent::class.isInstance(parent))
        assert(Parent::class.isInstance(child))
        assert(!Child::class.isInstance(parent))
        assert(Child::class.isInstance(child))

        assert(parent.clazz().isInstance(parent))
        assert(parent.clazz().isInstance(child))

        assert(!child.clazz().isInstance(parent))
        assert(child.clazz().isInstance(child))
    }

}
