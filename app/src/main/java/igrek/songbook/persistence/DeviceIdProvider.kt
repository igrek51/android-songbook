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
    private val logger = LoggerFactory.logger

    fun getUserId(): String {
        if (preferencesState.userDeviceId.isBlank()) {
            val uuid = newUUID()
            preferencesState.userDeviceId = uuid
            logger.debug("Unique User ID assigned: $uuid")

            preferencesService.dumpAll()
            userDataDao.requestSave(true)
        }
        return preferencesState.userDeviceId
    }

    // used to distinguish different devices in Editor Session and SongCast
    fun getUniqueDeviceId(): String {
        if (userDataDao.deviceDao.deviceDb.uid.isBlank()) {
            val uuid = newUUID()
            userDataDao.deviceDao.deviceDb.uid = uuid
            logger.debug("Unique Device ID assigned: $uuid")
            userDataDao.requestSave(true)
        }
        return userDataDao.deviceDao.deviceDb.uid
    }

    fun setUniqueDeviceId(udid: String) {
        userDataDao.deviceDao.deviceDb.uid = udid
        logger.info("Unique Device ID re-assigned: $udid")
        userDataDao.requestSave(true)
    }

    fun newUUID(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}
