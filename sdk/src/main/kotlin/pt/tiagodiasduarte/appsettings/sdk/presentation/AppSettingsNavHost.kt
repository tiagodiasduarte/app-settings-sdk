package pt.tiagodiasduarte.appsettings.sdk.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import pt.tiagodiasduarte.appsettings.sdk.R
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.AppSettingsRoute

/**
 * The screens of the App Settings Activity: Settings, the logs list and a log's detail.
 * Back pops the stack. On Settings, the system's default back closes the Activity.
 */
@Composable
internal fun AppSettingsNavHost(container: SdkContainer) {
    val backStack = rememberSaveable(saver = backStackSaver) {
        mutableStateListOf<AppSettingsRoute>(AppSettingsRoute.Settings)
    }
    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<AppSettingsRoute.Settings> {
                val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(uiState, onNavigate = { backStack.add(it) })
            }
            entry<AppSettingsRoute.Logs> {
                PendingScreen(stringResource(R.string.appsettings_logs_title))
            }
            entry<AppSettingsRoute.LogDetail> {
                PendingScreen(stringResource(R.string.appsettings_logs_title))
            }
        },
    )
}

/** Stand-in until the logs list and detail screens exist. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingScreen(title: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { _ -> }
}

private val backStackSaver: Saver<SnapshotStateList<AppSettingsRoute>, ArrayList<String>> = Saver(
    save = { stack -> ArrayList(stack.map(AppSettingsRoute::encode)) },
    restore = { saved -> mutableStateListOf<AppSettingsRoute>().apply { addAll(saved.map(AppSettingsRoute::decode)) } },
)
