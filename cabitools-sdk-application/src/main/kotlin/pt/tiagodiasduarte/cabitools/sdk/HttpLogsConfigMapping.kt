package pt.tiagodiasduarte.cabitools.sdk

import pt.tiagodiasduarte.cabitools.sdk.data.http.HeaderRules

/** Maps the public header options onto the infrastructure's header rules. */
internal fun HttpLogsConfig.toHeaderRules(): HeaderRules = HeaderRules(
    redactDefaultHeaders = redactDefaultHeaders,
    redactedHeaders = redactedHeaders,
    hiddenHeaders = hiddenHeaders,
)
