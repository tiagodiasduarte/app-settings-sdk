package pt.tiagodiasduarte.cabitools.sdk.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.cabitools.sdk.di.SdkContainer
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.CabiToolsRoute
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabiToolsTheme

@Config(qualifiers = "w411dp-h891dp")
@RunWith(AndroidJUnit4::class)
class CabiToolsNavHostTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun showNavHost() {
        composeRule.setContent {
            CabiToolsTheme(dynamicColor = false) {
                CabiToolsNavHost(SdkContainer(composeRule.activity))
            }
        }
        composeRule.waitUntil(TIMEOUT_MS) {
            composeRule.onAllNodesWithText("Open Logs").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun pressBack() = composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

    @Test
    fun `given the settings screen when Open Logs is selected then the logs screen is shown`() {
        // Given
        showNavHost()

        // When
        composeRule.onNodeWithText("Open Logs").performClick()

        // Then
        composeRule.onNodeWithText("HTTP logs").assertIsDisplayed()
    }

    @Test
    fun `given the logs screen when back is pressed then the settings screen is shown again`() {
        // Given
        showNavHost()
        composeRule.onNodeWithText("Open Logs").performClick()
        composeRule.onNodeWithText("HTTP logs").assertIsDisplayed()

        // When
        pressBack()

        // Then
        composeRule.onNodeWithText("Open Logs").assertIsDisplayed()
        assertFalse(composeRule.activity.isFinishing)
    }

    @Test
    fun `given the settings screen when back is pressed then the activity closes`() {
        // Given
        showNavHost()

        // When
        pressBack()
        composeRule.waitForIdle()

        // Then
        assertTrue(composeRule.activity.isFinishing)
    }

    @Test
    fun `given routes when they are saved and restored then the same back stack comes back`() {
        // Given
        val routes = listOf(CabiToolsRoute.Settings, CabiToolsRoute.Logs, CabiToolsRoute.LogDetail(42L))

        // When
        val restored = routes.map(CabiToolsRoute::encode).map(CabiToolsRoute::decode)

        // Then
        assertEquals(routes, restored)
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
