package pt.tiagodiasduarte.appsettings.sdk.presentation.model

import androidx.navigation3.runtime.NavKey

/** The screens of the App Settings Activity, used as Navigation 3 back stack keys. */
internal sealed interface AppSettingsRoute : NavKey {
    data object Settings : AppSettingsRoute
    data object Logs : AppSettingsRoute
    data class LogDetail(val id: Long) : AppSettingsRoute

    companion object {
        private const val LOG_DETAIL_PREFIX = "log:"

        /** A stable string for saving the back stack across configuration changes and process death. */
        fun encode(route: AppSettingsRoute): String = when (route) {
            Settings -> "settings"
            Logs -> "logs"
            is LogDetail -> "$LOG_DETAIL_PREFIX${route.id}"
        }

        fun decode(value: String): AppSettingsRoute = when {
            value == "logs" -> Logs
            value.startsWith(LOG_DETAIL_PREFIX) -> LogDetail(value.removePrefix(LOG_DETAIL_PREFIX).toLong())
            else -> Settings
        }
    }
}
