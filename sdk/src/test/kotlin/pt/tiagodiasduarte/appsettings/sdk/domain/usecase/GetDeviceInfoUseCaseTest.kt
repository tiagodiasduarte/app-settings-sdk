package pt.tiagodiasduarte.appsettings.sdk.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.appsettings.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.DeviceInfoRepository

class GetDeviceInfoUseCaseTest {

    private fun useCase(info: DeviceInfo) = GetDeviceInfoUseCase(
        object : DeviceInfoRepository {
            override fun getDeviceInfo() = info
        },
    )

    private fun device(manufacturer: String, model: String, release: String = "15", api: Int = 35) =
        DeviceInfo(manufacturer, model, release, api)

    @Test
    fun `combines and capitalizes manufacturer when model does not include it`() {
        val details = useCase(device("samsung", "SM-S918B"))()
        assertEquals("Samsung SM-S918B", details.model)
    }

    @Test
    fun `does not repeat manufacturer already present in model`() {
        val details = useCase(device("Google", "Google Pixel 8"))()
        assertEquals("Google Pixel 8", details.model)
    }

    @Test
    fun `manufacturer prefix match ignores case`() {
        val details = useCase(device("google", "Google Pixel 8"))()
        assertEquals("Google Pixel 8", details.model)
    }

    @Test
    fun `blank manufacturer falls back to model`() {
        val details = useCase(device("", "Pixel 8"))()
        assertEquals("Pixel 8", details.model)
    }

    @Test
    fun `formats android release with api level`() {
        val details = useCase(device("Google", "Pixel 8", release = "15", api = 35))()
        assertEquals("15 (API 35)", details.androidVersion)
    }
}
