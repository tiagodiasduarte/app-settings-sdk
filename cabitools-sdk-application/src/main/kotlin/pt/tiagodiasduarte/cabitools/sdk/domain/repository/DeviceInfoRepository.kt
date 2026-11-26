package pt.tiagodiasduarte.cabitools.sdk.domain.repository

import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo

internal interface DeviceInfoRepository {
    fun getDeviceInfo(): DeviceInfo
}
