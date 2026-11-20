package pt.tiagodiasduarte.appsettings.sdk.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.appsettings.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.appsettings.sdk.domain.repository.AppInfoRepository

class GetAppInfoUseCaseTest {

    private fun useCase(info: AppInfo) = GetAppInfoUseCase(
        object : AppInfoRepository {
            override fun getAppInfo() = info
        },
    )

    @Test
    fun `returns host app info from repository`() {
        val info = AppInfo("com.example.host", "2.3.0", 230L)
        assertEquals(info, useCase(info)())
    }

    @Test
    fun `keeps a missing version name as null`() {
        val info = AppInfo("com.example.host", null, 1L)
        assertEquals(null, useCase(info)().versionName)
    }

    @Test
    fun `keeps version codes larger than 32 bits`() {
        val info = AppInfo("com.example.host.debug", "1.0", 3_000_000_000L)
        assertEquals(3_000_000_000L, useCase(info)().versionCode)
    }
}
