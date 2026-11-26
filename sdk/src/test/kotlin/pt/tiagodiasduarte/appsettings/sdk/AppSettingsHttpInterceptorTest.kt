package pt.tiagodiasduarte.appsettings.sdk

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.appsettings.sdk.data.http.HttpLogsInterceptor
import pt.tiagodiasduarte.appsettings.sdk.data.local.HttpLogsStore

@RunWith(AndroidJUnit4::class)
class AppSettingsHttpInterceptorTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `given a retention option when the interceptor is created then it records into the shared store`() {
        // Given
        val configure: HttpLogsConfig.() -> Unit = { retention = Retention.ONE_WEEK }

        // When
        val interceptor = AppSettings.httpInterceptor(context, configure)

        // Then
        assertTrue(interceptor is HttpLogsInterceptor)
        assertEquals(Retention.ONE_WEEK, HttpLogsStore.get(context).retention)
    }

    @Test
    fun `given two interceptors with different retention when both are created then the latest applies`() {
        // Given
        AppSettings.httpInterceptor(context) { retention = Retention.ONE_WEEK }

        // When
        AppSettings.httpInterceptor(context) { retention = Retention.ONE_HOUR }

        // Then
        assertEquals(Retention.ONE_HOUR, HttpLogsStore.get(context).retention)
    }
}
