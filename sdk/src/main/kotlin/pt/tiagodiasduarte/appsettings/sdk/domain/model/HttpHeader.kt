package pt.tiagodiasduarte.appsettings.sdk.domain.model

/** One HTTP header line. Kept as a list, not a map, so repeated headers such as `Set-Cookie` survive. */
internal data class HttpHeader(
    val name: String,
    val value: String,
)
