package pt.tiagodiasduarte.appsettings.sdk.data

import android.content.Context
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo

@RunWith(AndroidJUnit4::class)
class AppInfoRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun installHostPackage() {
        val packageInfo = PackageInfo().apply {
            packageName = context.packageName
            versionName = "2.3.0"
            longVersionCode = 230L
        }
        shadowOf(context.packageManager).installPackage(packageInfo)
    }

    @Test
    @Config(sdk = [33])
    fun `reads the host package info on API 33 and above`() {
        assertEquals(expected(), AppInfoRepositoryImpl(context).getAppInfo())
    }

    @Test
    @Config(sdk = [32])
    fun `reads the host package info below API 33`() {
        assertEquals(expected(), AppInfoRepositoryImpl(context).getAppInfo())
    }

    private fun expected() = AppInfo(applicationId = context.packageName, versionName = "2.3.0", versionCode = 230L)
}
