package pt.tiagodiasduarte.cabitools.sdk.data.http

import pt.tiagodiasduarte.cabitools.sdk.data.local.HttpLogEntity

/** Receives finished calls from the interceptor. Must not block or throw. */
internal fun interface HttpLogSink {
    fun save(log: HttpLogEntity, requestBody: String?, responseBody: String?)
}
