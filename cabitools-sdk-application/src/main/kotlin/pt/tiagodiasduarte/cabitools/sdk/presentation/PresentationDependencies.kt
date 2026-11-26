package pt.tiagodiasduarte.cabitools.sdk.presentation

import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCase

/** What the Cabitools screens need. Provided by the module that assembles the SDK. */
internal interface PresentationDependencies {
    val getAppInfo: GetAppInfoUseCase
    val getDeviceInfo: GetDeviceInfoUseCase
    val httpLogsRepository: HttpLogsRepository
}
