package pt.tiagodiasduarte.appsettings.sdk.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer
import pt.tiagodiasduarte.appsettings.sdk.presentation.theme.AppSettingsTheme

/** Hosts the App Settings screens. Not exported: only reachable through `AppSettings.open`. */
internal class AppSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = SdkContainer(applicationContext)
        setContent {
            AppSettingsTheme {
                AppSettingsNavHost(container)
            }
        }
    }
}
