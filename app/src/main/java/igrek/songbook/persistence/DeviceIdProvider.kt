package igrek.songbook.persistence

import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.preferences.PreferencesState
import java.util.UUID

class DeviceIdProvider (
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val preferencesState by LazyExtractor(preferencesState)
    private val preferencesService by LazyExtractor(appFactory.preferencesService)
    private val userDataDao by LazyExtractor(appFactory.userDataDao)

    fun getDeviceId(): String {
        if (preferencesState.deviceId.isBlank()) {
            val uuid = newUUID()
            preferencesState.deviceId = uuid
            LoggerFactory.logger.debug("Device UUID assigned: $uuid")

            preferencesService.dumpAll()
            userDataDao.requestSave(true)
        }
        return preferencesState.deviceId
    }

    fun newUUID(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
