package igrek.songbook.dagger

import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory

object DaggerIoc {

    lateinit var factoryComponent: FactoryComponent

    private val logger = LoggerFactory.logger

    private var factoryModule: FactoryModule? = null

    fun init(activity: AppCompatActivity) {
        logger.info("Initializing Dependencies container...")
        factoryModule = FactoryModule(activity)
        factoryComponent = DaggerFactoryComponent.builder()
                .factoryModule(factoryModule)
                .build()
    }
}
