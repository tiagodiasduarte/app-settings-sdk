package pt.tiagodiasduarte.appsettings.sdk.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.tiagodiasduarte.appsettings.sdk.R
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer
import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceDetails
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetDeviceInfoUseCase
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.SectionUi
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.SettingsUiState
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.UiText

internal class SettingsViewModel(
    private val getAppInfo: GetAppInfoUseCase,
    private val getDeviceInfo: GetDeviceInfoUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sections = withContext(dispatcher) {
                // Order is part of the contract: Application first, then Device.
                listOf(
                    getAppInfo().toSection(),
                    getDeviceInfo().toSection(),
                )
            }
            _uiState.value = SettingsUiState(sections = sections)
        }
    }

    companion object {
        fun factory(container: SdkContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container.getAppInfo, container.getDeviceInfo) }
        }
    }
}

private fun AppInfo.toSection() = SectionUi(
    titleRes = R.string.appsettings_section_application,
    rows = listOf(
        InfoRowUi(
            R.string.appsettings_label_version_name,
            versionName?.let { UiText.Plain(it) } ?: UiText.Resource(R.string.appsettings_value_unknown),
        ),
        InfoRowUi(R.string.appsettings_label_version_code, UiText.Plain(versionCode.toString())),
        InfoRowUi(R.string.appsettings_label_app_id, UiText.Plain(applicationId)),
    ),
)

private fun DeviceDetails.toSection() = SectionUi(
    titleRes = R.string.appsettings_section_device,
    rows = listOf(
        InfoRowUi(R.string.appsettings_label_model, UiText.Plain(model)),
        InfoRowUi(R.string.appsettings_label_android_version, UiText.Plain(androidVersion)),
    ),
)
