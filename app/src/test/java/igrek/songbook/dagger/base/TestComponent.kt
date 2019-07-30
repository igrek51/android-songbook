package igrek.songbook.dagger.base

import dagger.Component
import igrek.songbook.dagger.DaggerInjectionTest
import igrek.songbook.dagger.FactoryComponent
import igrek.songbook.dagger.FactoryModule
import javax.inject.Singleton

@Singleton
@Component(modules = [FactoryModule::class])
interface TestComponent : FactoryComponent {

    // to use dagger injection in tests
    fun inject(there: DaggerInjectionTest)

    fun inject(there: BaseDaggerTest)

}