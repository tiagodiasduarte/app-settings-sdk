package pt.tiagodiasduarte.appsettings.sdk

import android.content.Context
import okhttp3.Interceptor

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

    /**
     * Returns an interceptor that passes every call through unchanged and records nothing.
     * The configuration block still runs, so invalid options fail the same way as in `:sdk`.
     */
    @JvmStatic
    @Suppress("UNUSED_PARAMETER", "UnusedParameter")
    public fun httpInterceptor(context: Context, configure: HttpLogsConfig.() -> Unit = {}): Interceptor {
        HttpLogsConfig().apply(configure)
        return Interceptor { chain -> chain.proceed(chain.request()) }
    }
}
