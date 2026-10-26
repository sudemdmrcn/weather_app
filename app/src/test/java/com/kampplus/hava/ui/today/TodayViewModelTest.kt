package com.kampplus.hava.ui.today

import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.testing.FakePreferencesRepository
import com.kampplus.hava.testing.FakeWeatherRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Bugün ekranının yükleme akışı: seçili şehrin tahmini, hata, tekrar deneme ve şehir değişimi.
 * `uiState` yalnızca dinlenirken çalıştığı için testler onu arka planda toplar.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val other = sampleCity(id = 99L, name = "Ankara")

    private fun TestScope.create(
        preferences: FakePreferencesRepository = FakePreferencesRepository(listOf(DEFAULT_CITY, other)),
        weather: FakeWeatherRepository = FakeWeatherRepository()
    ) = TodayViewModel(preferences, weather).also { viewModel ->
        backgroundScope.launch { viewModel.uiState.collect { } }
        backgroundScope.launch { viewModel.unit.collect { } }
    }

    private fun TodayViewModel.content() = (uiState.value as LoadState.Success).data

    @Test
    fun `acilista ilk kayitli sehrin tahmini yuklenir`() = runTest {
        val viewModel = create()
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(DEFAULT_CITY, content.city)
        assertTrue("İlk kayıtlı şehir Konumum sayılır", content.isHome)
    }

    @Test
    fun `baslangicta yukleniyor durumundadir`() = runTest {
        val viewModel = create()

        assertEquals(LoadState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        val viewModel = create(weather = FakeWeatherRepository(error = IOException("yok")))
        advanceUntilIdle()

        assertEquals(LoadState.Error("İnternet bağlantını kontrol et."), viewModel.uiState.value)
    }

    @Test
    fun `eksik current blogu hata olur`() = runTest {
        val viewModel = create(weather = FakeWeatherRepository(error = IllegalStateException("current yok")))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Error)
    }

    @Test
    fun `tekrar dene hatadan sonra yeniden yukler`() = runTest {
        val weather = FakeWeatherRepository(error = IOException("yok"))
        val viewModel = create(weather = weather)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoadState.Error)

        weather.error = null
        viewModel.onRetry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoadState.Success)
        assertEquals(2, weather.callCount)
    }

    @Test
    fun `baska sehir secilince o sehrin tahmini yuklenir`() = runTest {
        val preferences = FakePreferencesRepository(listOf(DEFAULT_CITY, other))
        val weather = FakeWeatherRepository()
        val viewModel = create(preferences, weather)
        advanceUntilIdle()

        preferences.selectCity(other.id)
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(other, content.city)
        assertFalse("Listenin ilk şehri olmayan Konumum sayılmaz", content.isHome)
        assertEquals(2, weather.callCount)
    }

    @Test
    fun `secilen saat sehir degisince basa doner`() = runTest {
        val preferences = FakePreferencesRepository(listOf(DEFAULT_CITY, other))
        val viewModel = create(preferences)
        advanceUntilIdle()

        viewModel.onHourSelected(5)
        assertEquals(5, viewModel.selectedHour.value)

        preferences.selectCity(other.id)
        advanceUntilIdle()

        assertEquals(0, viewModel.selectedHour.value)
    }

    @Test
    fun `birim degisimi yeniden istek atmaz ve kalicidir`() = runTest {
        val preferences = FakePreferencesRepository()
        val weather = FakeWeatherRepository()
        val viewModel = create(preferences, weather)
        advanceUntilIdle()

        viewModel.onUnitChange(TemperatureUnit.FAHRENHEIT)
        advanceUntilIdle()

        assertEquals(TemperatureUnit.FAHRENHEIT, viewModel.unit.value)
        // Veri hep Celsius gelir; yalnızca gösterim değişir, ağ isteği tekrarlanmaz.
        assertEquals(1, weather.callCount)
    }
}
