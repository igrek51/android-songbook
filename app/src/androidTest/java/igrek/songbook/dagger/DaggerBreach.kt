package igrek.songbook.dagger

object DaggerBreach {

    fun factory(): FactoryModule {
        val field = DaggerIoc::class.java.getDeclaredField("factoryModule")
        field.isAccessible = true
        val factoryModule = field.get(DaggerIoc::class) as FactoryModule?
        return factoryModule!!
    }

    fun component(): FactoryComponent {
        val field = DaggerIoc::class.java.getDeclaredField("factoryComponent")
        field.isAccessible = true
        val fieldValue = field.get(DaggerIoc::class) as FactoryComponent?
        return fieldValue!!
    }

    fun <T> inject(providerName: String): T {
        val component: FactoryComponent = component()
        val daggerComponent: DaggerFactoryComponent = component as DaggerFactoryComponent
        val field = daggerComponent::class.java.getDeclaredField(providerName)
        field.isAccessible = true
        val provider = field.get(daggerComponent)!!
        val field2 = provider::class.java.getDeclaredField("instance")
        field2.isAccessible = true
        val serviceValue = field2.get(provider)!!
        return serviceValue as T
    }

}