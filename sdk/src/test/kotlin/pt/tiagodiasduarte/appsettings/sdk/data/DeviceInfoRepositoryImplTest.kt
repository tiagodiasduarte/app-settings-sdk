package pt.tiagodiasduarte.appsettings.sdk.data

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo

class DeviceInfoRepositoryImplTest {

    @Test
    fun `maps build source values into device info`() {
        val source = object : DeviceBuildSource {
            override val manufacturer = "samsung"
            override val model = "SM-S918B"
            override val release = "14"
            override val sdkInt = 34
        }

        val info = DeviceInfoRepositoryImpl(source).getDeviceInfo()

        assertEquals(DeviceInfo("samsung", "SM-S918B", "14", 34), info)
    }
}
