package pt.tiagodiasduarte.cabitools.sdk.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pt.tiagodiasduarte.cabitools.sdk.data.local.HttpLogSummary
import pt.tiagodiasduarte.cabitools.sdk.data.local.HttpLogWithBodies
import pt.tiagodiasduarte.cabitools.sdk.data.local.HttpLogsStore
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpLogItem
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpMessage
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository

internal class HttpLogsRepositoryImpl(private val store: HttpLogsStore) : HttpLogsRepository {

    override fun observeLogs(): Flow<List<HttpLogItem>> =
        store.observeSummaries().map { summaries -> summaries.map { it.toItem() } }

    override suspend fun getLog(id: Long): HttpLogDetail? = store.get(id)?.toDetail()

    override suspend fun clearLogs() = store.clear()
}

private fun HttpLogSummary.toItem() = HttpLogItem(
    id = id,
    method = method,
    url = url,
    startedAt = requestedAt,
    durationMs = durationMs,
    statusCode = responseCode,
    error = error,
    responseSize = responseBodySize,
)

private fun HttpLogWithBodies.toDetail() = HttpLogDetail(
    id = log.id,
    method = log.method,
    url = log.url,
    startedAt = log.requestedAt,
    durationMs = log.durationMs,
    protocol = log.protocol,
    statusCode = log.responseCode,
    statusMessage = log.responseMessage,
    error = log.error,
    request = HttpMessage(
        headers = log.requestHeaders,
        contentType = log.requestContentType,
        body = bodies?.requestBody,
        bodySize = log.requestBodySize,
        bodyTruncated = log.requestBodyTruncated,
    ),
    response = HttpMessage(
        headers = log.responseHeaders,
        contentType = log.responseContentType,
        body = bodies?.responseBody,
        bodySize = log.responseBodySize,
        bodyTruncated = log.responseBodyTruncated,
    ),
)
