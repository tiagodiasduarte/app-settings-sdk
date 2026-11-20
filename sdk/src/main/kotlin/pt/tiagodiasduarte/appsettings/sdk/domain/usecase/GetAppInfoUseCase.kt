package pt.tiagodiasduarte.appsettings.sdk.domain.usecase

import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.AppInfoRepository

internal class GetAppInfoUseCase(
    private val repository: AppInfoRepository,
) {
    operator fun invoke(): AppInfo = repository.getAppInfo()
}
