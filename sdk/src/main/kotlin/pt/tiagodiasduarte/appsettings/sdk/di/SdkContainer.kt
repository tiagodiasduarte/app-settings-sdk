package pt.tiagodiasduarte.appsettings.sdk.di

import android.content.Context
import pt.tiagodiasduarte.appsettings.sdk.data.AppInfoRepositoryImpl
import pt.tiagodiasduarte.appsettings.sdk.data.DeviceInfoRepositoryImpl
import pt.tiagodiasduarte.appsettings.sdk.data.HttpLogsRepositoryImpl
import pt.tiagodiasduarte.appsettings.sdk.data.local.HttpLogsStore
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.AppInfoRepository
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.DeviceInfoRepository
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetDeviceInfoUseCase

/** Manual dependency wiring; no DI framework is imposed on host apps. */
internal class SdkContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    private val appInfoRepository: AppInfoRepository by lazy { AppInfoRepositoryImpl(appContext) }
    private val deviceInfoRepository: DeviceInfoRepository by lazy { DeviceInfoRepositoryImpl() }

    val getAppInfo: GetAppInfoUseCase by lazy { GetAppInfoUseCase(appInfoRepository) }
    val getDeviceInfo: GetDeviceInfoUseCase by lazy { GetDeviceInfoUseCase(deviceInfoRepository) }

    val httpLogsRepository: HttpLogsRepository by lazy { HttpLogsRepositoryImpl(HttpLogsStore.get(appContext)) }
}
