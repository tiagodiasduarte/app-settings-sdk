package pt.tiagodiasduarte.cabitools.sdk.data

import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository

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
