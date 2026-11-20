package pt.tiagodiasduarte.appsettings.sdk.domain.usecase

import java.util.Locale
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceDetails
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.DeviceInfoRepository

internal class GetDeviceInfoUseCase(
    private val repository: DeviceInfoRepository,
) {
    operator fun invoke(): DeviceDetails = repository.getDeviceInfo().toDetails()
}

internal fun DeviceInfo.toDetails(): DeviceDetails = DeviceDetails(
    model = displayModel(manufacturer, model),
    androidVersion = "$androidRelease (API $apiLevel)",
)

private fun displayModel(manufacturer: String, model: String): String {
    val maker = manufacturer.trim()
    val name = model.trim()
    return when {
        maker.isEmpty() -> name
        name.startsWith(maker, ignoreCase = true) -> name
        else -> "${maker.replaceFirstChar { it.titlecase(Locale.ROOT) }} $name"
    }
}
