package igrek.songbook.dagger

import android.support.v7.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory

object DaggerIoc {

    lateinit var factoryComponent: FactoryComponent

    private val logger = LoggerFactory.logger

    fun init(activity: AppCompatActivity) {
        logger.info("Initializing Dagger IOC container...")
        factoryComponent = DaggerFactoryComponent.builder()
                .factoryModule(FactoryModule(activity))
                .build()
    }
}
