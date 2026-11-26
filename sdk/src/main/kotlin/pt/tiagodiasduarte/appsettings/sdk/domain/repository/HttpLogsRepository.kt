package pt.tiagodiasduarte.appsettings.sdk.domain.repository

import kotlinx.coroutines.flow.Flow
import pt.tiagodiasduarte.appsettings.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.appsettings.sdk.domain.model.HttpLogItem

internal interface HttpLogsRepository {
    /** Logs within the retention period, newest first, updated as calls finish. */
    fun observeLogs(): Flow<List<HttpLogItem>>

    suspend fun getLog(id: Long): HttpLogDetail?

    suspend fun clearLogs()
}
