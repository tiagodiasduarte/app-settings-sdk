package pt.tiagodiasduarte.cabitools.sdk.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pt.tiagodiasduarte.cabitools.sdk.di.SdkContainer
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabiToolsTheme

/** Hosts the CabiTools screens. Not exported: only reachable through `CabiTools.open`. */
internal class CabiToolsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = SdkContainer(applicationContext)
        setContent {
            CabiToolsTheme {
                CabiToolsNavHost(container)
            }
        }
    }
}
