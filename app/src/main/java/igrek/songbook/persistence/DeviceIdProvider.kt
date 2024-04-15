package igrek.songbook.persistence

import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.preferences.SettingsState
import java.util.UUID

class DeviceIdProvider (
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
) {
    private val preferencesState by LazyExtractor(settingsState)
    private val preferencesService by LazyExtractor(appFactory.preferencesService)
    private val userDataDao by LazyExtractor(appFactory.userDataDao)

    fun getUserDeviceId(): String {
        if (preferencesState.userDeviceId.isBlank()) {
            val uuid = newUUID()
            preferencesState.userDeviceId = uuid
            LoggerFactory.logger.debug("Unique User ID assigned: $uuid")

            preferencesService.dumpAll()
            userDataDao.requestSave(true)
        }
        return preferencesState.userDeviceId
    }

    fun getUniqueDeviceId(): String {
        if (userDataDao.deviceDao.deviceDb.uid.isBlank()) {
            val uuid = newUUID()
            userDataDao.deviceDao.deviceDb.uid = uuid
            LoggerFactory.logger.debug("Unique Device ID assigned: $uuid")
            userDataDao.requestSave(true)
        }
        return userDataDao.deviceDao.deviceDb.uid
    }

    fun newUUID(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
