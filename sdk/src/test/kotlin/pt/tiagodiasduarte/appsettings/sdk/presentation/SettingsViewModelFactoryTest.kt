package pt.tiagodiasduarte.appsettings.sdk.presentation

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.appsettings.sdk.di.SdkContainer

@RunWith(AndroidJUnit4::class)
class SettingsViewModelFactoryTest {

    @Test
    fun `factory creates the view model from the SDK container`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val factory = SettingsViewModel.factory(SdkContainer(context))

        val viewModel = ViewModelProvider.create(ViewModelStore(), factory)[SettingsViewModel::class]

        assertNotNull(viewModel.uiState.value)
    }
}
