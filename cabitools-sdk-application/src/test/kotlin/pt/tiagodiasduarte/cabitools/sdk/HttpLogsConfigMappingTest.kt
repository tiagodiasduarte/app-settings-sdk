package pt.tiagodiasduarte.cabitools.sdk

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.HttpHeader

class HttpLogsConfigMappingTest {

    @Test
    fun `given header options when they are mapped then the rules redact and hide those headers`() {
        // Given
        val config = HttpLogsConfig().apply {
            redactHeaders("X-Api-Key")
            hideHeaders("X-Internal-Trace")
        }

        // When
        val rules = config.toHeaderRules()

        // Then
        assertEquals(
            listOf(HttpHeader("Authorization", "****"), HttpHeader("X-Api-Key", "****"), HttpHeader("Accept", "*/*")),
            rules.apply(
                Headers.headersOf(
                    "Authorization", "Bearer a", "X-Api-Key", "k", "X-Internal-Trace", "t", "Accept", "*/*",
                ),
            ),
        )
    }

    @Test
    fun `given default redaction turned off when it is mapped then sensitive headers are kept`() {
        // Given
        val config = HttpLogsConfig().apply { redactDefaultHeaders = false }

        // When
        val rules = config.toHeaderRules()

        // Then
        assertEquals(listOf(HttpHeader("Cookie", "a=1")), rules.apply(Headers.headersOf("Cookie", "a=1")))
    }
}
