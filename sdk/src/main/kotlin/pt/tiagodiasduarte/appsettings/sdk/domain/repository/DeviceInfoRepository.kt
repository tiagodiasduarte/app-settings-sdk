package pt.tiagodiasduarte.appsettings.sdk.domain.repository

import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo

internal interface DeviceInfoRepository {
    fun getDeviceInfo(): DeviceInfo
}
