package pt.tiagodiasduarte.appsettings.sdk.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer
import pt.tiagodiasduarte.appsettings.sdk.presentation.theme.AppSettingsTheme

/** Hosts the settings screen. Not exported: only reachable through `AppSettings.open`. */
internal class AppSettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.factory(SdkContainer(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppSettingsTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(uiState)
            }
        }
    }
}
