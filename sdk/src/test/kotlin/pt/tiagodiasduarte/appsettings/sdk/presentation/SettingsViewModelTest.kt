package pt.tiagodiasduarte.appsettings.sdk.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.tiagodiasduarte.appsettings.sdk.R
import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.AppInfoRepository
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.DeviceInfoRepository
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.appsettings.sdk.domain.usecase.GetDeviceInfoUseCase
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.UiText

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        app: AppInfo = AppInfo("com.example.host", "2.3.0", 230L),
        device: DeviceInfo = DeviceInfo("samsung", "SM-S918B", "15", 35),
    ) = SettingsViewModel(
        getAppInfo = GetAppInfoUseCase(object : AppInfoRepository {
            override fun getAppInfo() = app
        }),
        getDeviceInfo = GetDeviceInfoUseCase(object : DeviceInfoRepository {
            override fun getDeviceInfo() = device
        }),
        dispatcher = dispatcher,
    )

    @Test
    fun `given loading has not run when the view model is created then it has no sections`() {
        // Given
        val app = AppInfo("com.example.host", "2.3.0", 230L)

        // When
        val vm = viewModel(app = app)

        // Then
        assertTrue(vm.uiState.value.sections.isEmpty())
    }

    @Test
    fun `given app and device info when loading finishes then sections are Application then Device`() =
        runTest(dispatcher) {
            // Given
            val app = AppInfo("com.example.host", "2.3.0", 230L)

            // When
            val vm = viewModel(app = app).also { advanceUntilIdle() }

            // Then
            assertEquals(
                listOf(R.string.appsettings_section_application, R.string.appsettings_section_device),
                vm.uiState.value.sections.map { it.titleRes },
            )
        }

    @Test
    fun `given host app info when loading finishes then the application section shows its values`() =
        runTest(dispatcher) {
            // Given
            val app = AppInfo("com.example.host", "2.3.0", 230L)

            // When
            val vm = viewModel(app = app).also { advanceUntilIdle() }

            // Then
            assertEquals(
                listOf(
                    InfoRowUi(R.string.appsettings_label_version_name, UiText.Plain("2.3.0")),
                    InfoRowUi(R.string.appsettings_label_version_code, UiText.Plain("230")),
                    InfoRowUi(R.string.appsettings_label_app_id, UiText.Plain("com.example.host")),
                ),
                vm.uiState.value.sections[0].rows,
            )
        }

    @Test
    fun `given a missing version name when loading finishes then it shows the unknown placeholder`() =
        runTest(dispatcher) {
            // Given
            val app = AppInfo("com.example.host", null, 1L)

            // When
            val vm = viewModel(app = app).also { advanceUntilIdle() }

            // Then
            assertEquals(
                UiText.Resource(R.string.appsettings_value_unknown),
                vm.uiState.value.sections[0].rows[0].value,
            )
        }

    @Test
    fun `given a version code larger than 32 bits when loading finishes then it is shown in full`() =
        runTest(dispatcher) {
            // Given
            val app = AppInfo("com.example.host", "1.0", 3_000_000_000L)

            // When
            val vm = viewModel(app = app).also { advanceUntilIdle() }

            // Then
            assertEquals(UiText.Plain("3000000000"), vm.uiState.value.sections[0].rows[1].value)
        }

    @Test
    fun `given device info when loading finishes then the device section shows model and android version`() =
        runTest(dispatcher) {
            // Given
            val device = DeviceInfo("samsung", "SM-S918B", "15", 35)

            // When
            val vm = viewModel(device = device).also { advanceUntilIdle() }

            // Then
            assertEquals(
                listOf(
                    InfoRowUi(R.string.appsettings_label_model, UiText.Plain("Samsung SM-S918B")),
                    InfoRowUi(R.string.appsettings_label_android_version, UiText.Plain("15 (API 35)")),
                ),
                vm.uiState.value.sections[1].rows,
            )
        }
}
