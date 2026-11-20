package pt.tiagodiasduarte.appsettings.sdk.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(AndroidJUnit4::class)
class AndroidDeviceBuildSourceTest {

    @Test
    @Config(sdk = [34])
    fun `reads the device fields from Build`() {
        ShadowBuild.setManufacturer("Google")
        ShadowBuild.setModel("Pixel 8")
        ShadowBuild.setVersionRelease("14")

        assertEquals("Google", AndroidDeviceBuildSource.manufacturer)
        assertEquals("Pixel 8", AndroidDeviceBuildSource.model)
        assertEquals("14", AndroidDeviceBuildSource.release)
        assertEquals(34, AndroidDeviceBuildSource.sdkInt)
    }
}
