package pt.tiagodiasduarte.appsettings

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `given two and two when they are added then the result is four`() {
        // Given
        val a = 2
        val b = 2

        // When
        val sum = a + b

        // Then
        assertEquals(4, sum)
    }
}
