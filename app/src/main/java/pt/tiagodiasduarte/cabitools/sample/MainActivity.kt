package pt.tiagodiasduarte.cabitools.sample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.tiagodiasduarte.cabitools.sdk.CabiTools
import pt.tiagodiasduarte.cabitools.sdk.Retention
import java.io.IOException

/** Sample host app: opens the CabiTools screen and makes HTTP calls that it records. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold { innerPadding ->
                    val context = LocalContext.current
                    val client = remember { sampleClient(context.applicationContext) }
                    val scope = rememberCoroutineScope()
                    var result by remember { mutableStateOf<String?>(null) }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(onClick = { CabiTools.open(context) }) {
                            Text("Open CabiTools")
                        }
                        OutlinedButton(onClick = { scope.launch { result = sampleRequest(client) } }) {
                            Text("Make sample request")
                        }
                        result?.let { Text(it) }
                    }
                }
            }
        }
    }
}

/** The app's own OkHttp client, as a Retrofit setup would build it, with the SDK interceptor added. */
private fun sampleClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        CabiTools.httpInterceptor(context) {
            retention = Retention.ONE_DAY
            maxContentLength = MAX_STORED_BODY_BYTES
            redactHeaders("X-Api-Key")
            hideHeaders("X-Internal-Trace")
        },
    )
    .build()

private suspend fun sampleRequest(client: OkHttpClient): String = withContext(Dispatchers.IO) {
    val request = Request.Builder()
        .url(SAMPLE_URL)
        .header("X-Api-Key", "sample-key")
        .header("X-Internal-Trace", "sample-trace")
        .build()
    try {
        // Read the body, as Retrofit does: a call is logged once its body is read or closed.
        client.newCall(request).execute().use { response ->
            "HTTP ${response.code}, ${response.body?.string().orEmpty().length} characters"
        }
    } catch (e: IOException) {
        "Request failed: ${e.message}"
    }
}

private const val SAMPLE_URL = "https://jsonplaceholder.typicode.com/todos/1"
private const val MAX_STORED_BODY_BYTES = 250_000L
