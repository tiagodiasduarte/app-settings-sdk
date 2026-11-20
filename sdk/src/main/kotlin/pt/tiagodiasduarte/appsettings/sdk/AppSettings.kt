package pt.tiagodiasduarte.appsettings.sdk

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import pt.tiagodiasduarte.appsettings.sdk.presentation.AppSettingsActivity

/**
 * Entry point of the App Settings SDK.
 *
 * ```
 * AppSettings.open(context)
 * ```
 */
public object AppSettings {

    /**
     * Opens the App Settings screen on top of the current screen.
     *
     * Any [Context] works. When it is not (or does not wrap) an [Activity], for example the
     * Application context, the screen is started with [Intent.FLAG_ACTIVITY_NEW_TASK].
     * Pressing back closes the screen and returns to the caller.
     */
    @JvmStatic
    public fun open(context: Context) {
        val intent = Intent(context, AppSettingsActivity::class.java)
        if (context.findActivity() == null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
