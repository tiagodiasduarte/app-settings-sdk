package pt.tiagodiasduarte.appsettings.sdk.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.appsettings.sdk.R
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.SectionUi
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.SettingsUiState
import pt.tiagodiasduarte.appsettings.sdk.presentation.model.UiText
import pt.tiagodiasduarte.appsettings.sdk.presentation.theme.AppSettingsTheme

// Phone-sized screen, so every row of the default state fits without scrolling.
@Config(qualifiers = "w411dp-h891dp")
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val state = SettingsUiState(
        sections = listOf(
            SectionUi(
                R.string.appsettings_section_application,
                listOf(
                    InfoRowUi(
                        R.string.appsettings_label_version_name,
                        UiText.Resource(R.string.appsettings_value_unknown),
                    ),
                    InfoRowUi(R.string.appsettings_label_version_code, UiText.Plain("230")),
                    InfoRowUi(R.string.appsettings_label_app_id, UiText.Plain("com.example.host")),
                ),
            ),
            SectionUi(
                R.string.appsettings_section_device,
                listOf(
                    InfoRowUi(R.string.appsettings_label_model, UiText.Plain("Samsung SM-S918B")),
                    InfoRowUi(R.string.appsettings_label_android_version, UiText.Plain("15 (API 35)")),
                ),
            ),
        ),
    )

    private fun setContent(uiState: SettingsUiState = state) =
        composeRule.setContent { AppSettingsTheme(dynamicColor = false) { SettingsScreen(uiState) } }

    @Test
    fun showsApplicationSectionBeforeDeviceSection() {
        setContent()

        val appTop = composeRule.onNodeWithText("Application").assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot.top
        val deviceTop = composeRule.onNodeWithText("Device").assertIsDisplayed()
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue("Application must be above Device", appTop < deviceTop)
    }

    @Test
    fun showsLabelsWithValues() {
        setContent()

        listOf(
            "Version name", "Unknown",
            "Version code", "230",
            "App id", "com.example.host",
            "Model", "Samsung SM-S918B",
            "Android version", "15 (API 35)",
        ).forEach { composeRule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun listScrollsToContentBeyondTheScreen() {
        val manyRows = List(40) { InfoRowUi(R.string.appsettings_label_model, UiText.Plain("value $it")) }
        setContent(SettingsUiState(sections = listOf(SectionUi(R.string.appsettings_section_device, manyRows))))

        composeRule.onNodeWithTag(SETTINGS_LIST_TAG).performScrollToNode(hasText("value 39"))
        composeRule.onNodeWithText("value 39").assertIsDisplayed()
    }
}
