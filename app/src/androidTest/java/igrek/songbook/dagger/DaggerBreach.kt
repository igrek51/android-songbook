package igrek.songbook.dagger

object DaggerBreach {

    fun factory(): FactoryModule {
        val field = DaggerIoc::class.java.getDeclaredField("factoryModule")
        field.isAccessible = true
        val factoryModule = field.get(DaggerIoc::class) as FactoryModule?
        return factoryModule!!
    }

}