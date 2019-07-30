package igrek.songbook.dagger

import dagger.Component
import igrek.songbook.wip.WIPFeatureTest
import javax.inject.Singleton

@Singleton
@Component(modules = [FactoryModule::class])
interface AndroidTestComponent : FactoryComponent {

    // enable dagger injection in tests
    fun inject(there: WIPFeatureTest)

}