package pt.tiagodiasduarte.appsettings.sdk.domain.repository

import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo

internal interface AppInfoRepository {
    fun getAppInfo(): AppInfo
}
