package pt.tiagodiasduarte.cabitools.sdk.domain.usecase

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository

internal class GetAppInfoUseCase(
    private val repository: AppInfoRepository,
) {
    operator fun invoke(): AppInfo = repository.getAppInfo()
}
