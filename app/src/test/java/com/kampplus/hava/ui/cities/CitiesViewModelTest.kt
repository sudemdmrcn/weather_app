package com.kampplus.hava.ui.cities

import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.testing.FakePreferencesRepository
import com.kampplus.hava.testing.FakeWeatherRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Şehirler ekranı: özetlerin paralel yüklenmesi, tek şehir hatası, süzme ve şehir ekleme. */
@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ankara = sampleCity(id = 323786L, name = "Ankara").copy(latitude = 39.9)
    private val izmir = sampleCity(id = 311046L, name = "İzmir").copy(latitude = 38.4)

    private fun TestScope.create(
        preferences: FakePreferencesRepository = FakePreferencesRepository(listOf(DEFAULT_CITY, ankara, izmir)),
        weather: FakeWeatherRepository = FakeWeatherRepository()
    ) = CitiesViewModel(preferences, weather).also { viewModel ->
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private fun CitiesViewModel.cards() = (uiState.value as LoadState.Success).data

    @Test
    fun `kayitli her sehir icin ozet yuklenir`() = runTest {
        val weather = FakeWeatherRepository()
        val viewModel = create(weather = weather)
        advanceUntilIdle()

        val cards = viewModel.cards()
        assertEquals(listOf("İstanbul", "Ankara", "İzmir"), cards.map { it.city.name })
        assertTrue(cards.all { it.summary != null })
        assertEquals(3, weather.summaryCallCount)
    }

    @Test
    fun `yalnizca ilk sehir konumum sayilir`() = runTest {
        val viewModel = create()
        advanceUntilIdle()

        assertEquals(listOf(true, false, false), viewModel.cards().map { it.isHome })
    }

    @Test
    fun `tek sehrin hatasi listeyi bozmaz`() = runTest {
        val weather = FakeWeatherRepository(summaryFailures = mutableSetOf(ankara.latitude))
        val viewModel = create(weather = weather)
        advanceUntilIdle()

        val cards = viewModel.cards()
        assertEquals(3, cards.size)
        assertNull("Hatalı şehir özetsiz kalır", cards.first { it.city.id == ankara.id }.summary)
        assertNotNull(cards.first { it.city.id == izmir.id }.summary)
    }

    @Test
    fun `tum sehirler basarisizsa hata gosterilir`() = runTest {
        val viewModel = create(weather = FakeWeatherRepository(summaryError = IOException("yok")))
        advanceUntilIdle()

        assertEquals(LoadState.Error("İnternet bağlantını kontrol et."), viewModel.uiState.value)
    }

    @Test
    fun `arama kayitli sehirleri buyuk kucuk harfe bakmadan suzer`() = runTest {
        val viewModel = create()
        advanceUntilIdle()

        viewModel.onQueryChange("ank")
        advanceUntilIdle()

        assertEquals(listOf("Ankara"), viewModel.cards().map { it.city.name })
    }

    @Test
    fun `eslesme yoksa bos durum gosterilir`() = runTest {
        val viewModel = create()
        advanceUntilIdle()

        viewModel.onQueryChange("xyz")
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `arama internete yeniden istek atmaz`() = runTest {
        val weather = FakeWeatherRepository()
        val viewModel = create(weather = weather)
        advanceUntilIdle()

        viewModel.onQueryChange("an")
        advanceUntilIdle()

        assertEquals(3, weather.summaryCallCount)
    }

    @Test
    fun `sehir ekleyince kaydedilir ve secilir`() = runTest {
        val preferences = FakePreferencesRepository()
        val viewModel = create(preferences)

        viewModel.addAndSelect(ankara)

        assertEquals(listOf(DEFAULT_CITY, ankara), preferences.savedCities.first())
        assertEquals(ankara, preferences.selectedCity.first())
    }

    @Test
    fun `ayni sehir iki kez eklenmez`() = runTest {
        val preferences = FakePreferencesRepository()
        val viewModel = create(preferences)

        viewModel.addAndSelect(ankara)
        viewModel.addAndSelect(ankara)

        assertEquals(2, preferences.savedCities.first().size)
    }

    @Test
    fun `karta dokunmak sehri secer`() = runTest {
        val preferences = FakePreferencesRepository(listOf(DEFAULT_CITY, ankara))
        val viewModel = create(preferences)

        viewModel.onCityClick(ankara)
        advanceUntilIdle()

        assertEquals(ankara, preferences.selectedCity.first())
        assertFalse(preferences.selectedCity.first() == DEFAULT_CITY)
    }
}
