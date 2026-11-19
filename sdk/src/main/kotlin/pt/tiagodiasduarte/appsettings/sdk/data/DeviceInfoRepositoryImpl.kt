package pt.tiagodiasduarte.appsettings.sdk.data

import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.DeviceInfoRepository

internal class DeviceInfoRepositoryImpl(
    private val source: DeviceBuildSource = AndroidDeviceBuildSource,
) : DeviceInfoRepository {

    override fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = source.manufacturer,
        model = source.model,
        androidRelease = source.release,
        apiLevel = source.sdkInt,
    )
}
