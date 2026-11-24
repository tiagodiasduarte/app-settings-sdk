package pt.tiagodiasduarte.appsettings.sdk

import android.content.Context

/**
 * Production stand-in for the App Settings SDK entry point. It has the same API as `:sdk`,
 * so host code compiles in every build, but it does nothing.
 */
public object AppSettings {

    /** Does nothing: production builds have no App Settings screen. */
    @JvmStatic
    @Suppress("UNUSED_PARAMETER", "UnusedParameter")
    public fun open(context: Context) {
        // Intentionally empty.
    }
}
