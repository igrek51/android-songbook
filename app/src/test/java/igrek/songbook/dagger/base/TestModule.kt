package igrek.songbook.dagger.base

import androidx.appcompat.app.AppCompatActivity

import igrek.songbook.dagger.FactoryModule
import igrek.songbook.info.logger.Logger
import igrek.songbook.mock.LoggerMock

class TestModule(activity: AppCompatActivity) : FactoryModule(activity) {

    override fun provideLogger(): Logger {
        return LoggerMock()
    }

}
