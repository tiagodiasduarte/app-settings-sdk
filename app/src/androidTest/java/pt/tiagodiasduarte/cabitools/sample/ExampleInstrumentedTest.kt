package pt.tiagodiasduarte.cabitools.sample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun givenTheAppUnderTest_whenItsContextIsRead_thenThePackageNameIsTheSampleAppId() {
        // Given
        val instrumentation = InstrumentationRegistry.getInstrumentation()

        // When
        val appContext = instrumentation.targetContext

        // Then
        assertEquals("pt.tiagodiasduarte.cabitools.sample", appContext.packageName)
    }
}
