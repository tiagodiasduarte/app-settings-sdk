package pt.tiagodiasduarte.cabitools.sdk.domain.repository

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo

internal interface AppInfoRepository {
    fun getAppInfo(): AppInfo
}
