package pt.tiagodiasduarte.cabitools.sdk.presentation.model

import androidx.navigation3.runtime.NavKey

/** The screens of the CabiTools Activity, used as Navigation 3 back stack keys. */
internal sealed interface CabiToolsRoute : NavKey {
    data object Settings : CabiToolsRoute
    data object Logs : CabiToolsRoute
    data class LogDetail(val id: Long) : CabiToolsRoute

    companion object {
        private const val LOG_DETAIL_PREFIX = "log:"

        /** A stable string for saving the back stack across configuration changes and process death. */
        fun encode(route: CabiToolsRoute): String = when (route) {
            Settings -> "settings"
            Logs -> "logs"
            is LogDetail -> "$LOG_DETAIL_PREFIX${route.id}"
        }

        fun decode(value: String): CabiToolsRoute = when {
            value == "logs" -> Logs
            value.startsWith(LOG_DETAIL_PREFIX) -> LogDetail(value.removePrefix(LOG_DETAIL_PREFIX).toLong())
            else -> Settings
        }
    }
}
