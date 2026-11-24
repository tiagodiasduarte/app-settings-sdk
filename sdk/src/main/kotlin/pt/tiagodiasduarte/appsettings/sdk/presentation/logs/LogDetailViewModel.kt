package pt.tiagodiasduarte.appsettings.sdk.presentation.logs

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.tiagodiasduarte.appsettings.sdk.R
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer
import pt.tiagodiasduarte.appsettings.sdk.domain.model.ContentTypes
import pt.tiagodiasduarte.appsettings.sdk.domain.model.HttpHeader
import pt.tiagodiasduarte.appsettings.sdk.domain.model.HttpLogDetail
import pt.tiagodiasduarte.appsettings.sdk.domain.model.HttpMessage
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.HttpLogsRepository

/** A label and its value on the Overview tab. */
internal data class OverviewRowUi(@StringRes val labelRes: Int, val value: String)

/** How a stored body is shown. */
internal sealed interface BodyUi {
    /** [text] is pretty-printed when it is valid JSON. */
    data class Text(val text: String, val truncated: Boolean) : BodyUi
    data class Binary(val size: String) : BodyUi
    /** [size] is null when the app closed the body before reading it and its length was unknown. */
    data class NotCaptured(val size: String?) : BodyUi
    data object Empty : BodyUi
}

internal data class MessageUi(val headers: List<HttpHeader>, val body: BodyUi)

internal data class LogDetailUi(
    /** For example `GET /users/42`. */
    val title: String,
    val overview: List<OverviewRowUi>,
    val request: MessageUi,
    val response: MessageUi,
    /** The whole call as text, for the share action. */
    val shareText: String,
)

internal data class LogDetailUiState(
    val loaded: Boolean = false,
    /** Null once loaded means the log no longer exists (cleared or expired). */
    val detail: LogDetailUi? = null,
)

internal class LogDetailViewModel(
    private val id: Long,
    private val repository: HttpLogsRepository,
    private val format: LogFormat = LogFormat(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogDetailUiState())
    val uiState: StateFlow<LogDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = LogDetailUiState(loaded = true, detail = repository.getLog(id)?.toUi())
        }
    }

    private fun HttpLogDetail.toUi() = LogDetailUi(
        title = "$method ${url.toPath()}",
        overview = listOfNotNull(
            OverviewRowUi(R.string.appsettings_detail_url, url),
            OverviewRowUi(R.string.appsettings_detail_method, method),
            OverviewRowUi(R.string.appsettings_detail_status, status()),
            OverviewRowUi(R.string.appsettings_detail_started, format.timeWithMillis(startedAt)),
            OverviewRowUi(R.string.appsettings_detail_duration, format.duration(durationMs)),
            OverviewRowUi(R.string.appsettings_detail_protocol, protocol ?: NONE),
            OverviewRowUi(R.string.appsettings_detail_request_size, request.bodySize?.let(format::size) ?: NONE),
            OverviewRowUi(R.string.appsettings_detail_response_size, response.bodySize?.let(format::size) ?: NONE),
            error?.let { OverviewRowUi(R.string.appsettings_detail_error, it) },
        ),
        request = request.toUi(),
        response = response.toUi(),
        shareText = LogShareText.build(this, format),
    )

    private fun HttpLogDetail.status(): String =
        // HTTP/2 responses have no reason phrase, so the message is often blank.
        listOfNotNull(statusCode, statusMessage?.takeIf { it.isNotBlank() }).joinToString(" ").ifEmpty { NONE }

    private fun HttpMessage.toUi() = MessageUi(headers = headers, body = bodyUi())

    private fun HttpMessage.bodyUi(): BodyUi {
        val size = bodySize?.takeIf { it > 0 }
        return when {
            body != null -> BodyUi.Text(
                text = if (ContentTypes.isJson(contentType)) JsonPrettyPrinter.format(body) else body,
                truncated = bodyTruncated,
            )
            bodySize == null && contentType != null -> BodyUi.NotCaptured(size = null)
            size == null -> BodyUi.Empty
            ContentTypes.isText(contentType) -> BodyUi.NotCaptured(format.size(size))
            else -> BodyUi.Binary(format.size(size))
        }
    }

    /** Path and query, so the title stays short; the full URL is on the Overview tab. */
    private fun String.toPath(): String = substringAfter("://").let { rest ->
        rest.indexOf('/').takeIf { it >= 0 }?.let { rest.substring(it) } ?: "/"
    }

    companion object {
        private const val NONE = "-"

        fun factory(id: Long, container: SdkContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { LogDetailViewModel(id, container.httpLogsRepository) }
        }
    }
}
