package igrek.songbook.persistence.user.device

import kotlinx.serialization.Serializable

@Serializable
data class DeviceDb(
    var uid: String = "",
)
