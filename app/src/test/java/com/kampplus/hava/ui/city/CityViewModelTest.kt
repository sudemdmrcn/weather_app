package com.kampplus.hava.ui.city

import com.kampplus.hava.data.TurkishProvinces
import com.kampplus.hava.model.City
import com.kampplus.hava.testing.FakeCityRepository
import com.kampplus.hava.testing.MainDispatcherRule
import com.kampplus.hava.testing.sampleCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Şehir arama ekranı: harf yazdıkça süzme, yerel il listesi + ağ araması birleşimi ve dört durum.
 *
 * Temel kurallar: tek harfte ağ isteği atılmaz, iller anında görünür, ağ isteği yazmayı bıraktıktan
 * sonra tek sefer atılır, ağ hatası eşleşen illeri silmez.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCityRepository()

    private fun createViewModel() = CityViewModel(repository, debounceMs = 300)

    private val berlin = sampleCity(id = 2950159L, name = "Berlin").copy(countryCode = "DE", country = "Almanya")

    private fun CityViewModel.cities(): List<City> = (uiState.value as LoadState.Success).data

    private fun CityViewModel.names() = cities().map { it.name }

    @Test
    fun `acilista 81 il hazirdir ve ag istegi atilmaz`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(TurkishProvinces.all, viewModel.cities())
        assertEquals(81, viewModel.cities().size)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `tek harf yazinca o harfle baslayan iller listelenir`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("a")
        runCurrent()

        val names = viewModel.names()
        assertTrue("Adana listede olmalı: $names", "Adana" in names)
        assertTrue("Ankara listede olmalı: $names", "Ankara" in names)
        assertFalse("Bursa A ile başlamıyor: $names", "Bursa" in names)
        assertTrue(names.all { it.first().uppercaseChar() == 'A' || it.first() == 'Â' })
        assertEquals("Tek harfte internete gidilmez", 0, repository.callCount)
    }

    @Test
    fun `tek harf ag istegi atmaz sure gecse bile`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("a")
        advanceUntilIdle()

        assertEquals(0, repository.callCount)
    }

    @Test
    fun `turkce harfler ve buyuk kucuk harf yok sayilir`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("sanli")
        runCurrent()
        assertEquals(listOf("Şanlıurfa"), viewModel.names())

        viewModel.onQueryChange("ISPARTA")
        runCurrent()
        assertEquals(listOf("Isparta"), viewModel.names())

        viewModel.onQueryChange("i")
        runCurrent()
        assertTrue(viewModel.names().containsAll(listOf("İstanbul", "İzmir", "Isparta", "Iğdır")))
    }

    @Test
    fun `iki harften sonra iller hemen gorunur ag sonucu gecikmeli gelir`() = runTest {
        repository.result = listOf(berlin)
        val viewModel = createViewModel()

        viewModel.onQueryChange("an")
        runCurrent()

        // Yazma bitmeden yalnızca yerel iller var ve ağ isteği henüz atılmadı.
        assertTrue("Ankara hemen görünmeli: ${viewModel.names()}", "Ankara" in viewModel.names())
        assertEquals(0, repository.callCount)

        advanceTimeBy(299)
        runCurrent()
        assertEquals("300 ms dolmadan istek atılmaz", 0, repository.callCount)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(1, repository.callCount)
        assertEquals("an", repository.lastQuery)
        // İller önce, ağdan gelenler sonra.
        assertEquals("Berlin", viewModel.names().last())
        assertTrue("Ankara" in viewModel.names())
    }

    @Test
    fun `hizli yazarken yalnizca son metin icin istek atilir`() = runTest {
        repository.result = listOf(berlin)
        val viewModel = createViewModel()

        listOf("b", "be", "ber", "berl").forEach {
            viewModel.onQueryChange(it)
            runCurrent()
        }
        advanceUntilIdle()

        assertEquals(1, repository.callCount)
        assertEquals("berl", repository.lastQuery)
    }

    @Test
    fun `klavyedeki arama tusu beklemeden arar`() = runTest {
        repository.result = listOf(berlin)
        val viewModel = createViewModel()

        viewModel.onQueryChange("berlin")
        viewModel.search()
        runCurrent()

        assertEquals(1, repository.callCount)
        assertEquals(listOf("Berlin"), viewModel.names())
    }

    @Test
    fun `il eslesmesi yoksa ag cevabi beklenirken Loading gosterilir`() = runTest {
        repository.result = listOf(berlin)
        val viewModel = createViewModel()

        viewModel.onQueryChange("berlin")
        runCurrent()

        assertEquals(LoadState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(listOf("Berlin"), viewModel.names())
    }

    @Test
    fun `kocaeli ili dogru merkezle gelir ve ayni adli koy elenir`() = runTest {
        // Open-Meteo "Kocaeli" aratınca Balıkesir'deki küçük bir köyü döndürür.
        val village = sampleCity(id = 306970L, name = "Kocaeli").copy(latitude = 39.62497, longitude = 27.51145)
        repository.result = listOf(village)
        val viewModel = createViewModel()

        viewModel.onQueryChange("kocaeli")
        advanceUntilIdle()

        val result = viewModel.cities()
        assertEquals(1, result.size)
        assertEquals("Kocaeli ili İzmit merkezinin kimliğini taşımalı", 745028L, result.single().id)
        assertEquals(40.76499, result.single().latitude, 0.001)
    }

    @Test
    fun `ayni kayit hem ilden hem aramadan gelirse tekrar edilmez`() = runTest {
        repository.result = listOf(sampleCity(id = 323786L, name = "Ankara"))
        val viewModel = createViewModel()

        viewModel.onQueryChange("ankara")
        advanceUntilIdle()

        assertEquals(1, viewModel.names().count { it == "Ankara" })
    }

    @Test
    fun `arama temizlenince tum iller geri gelir`() = runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChange("an")
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals(81, viewModel.cities().size)
    }

    @Test
    fun `hicbir sey eslesmezse Empty gosterilir`() = runTest {
        repository.result = emptyList()
        val viewModel = createViewModel()

        viewModel.onQueryChange("xyzq")
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `tek harfte eslesen il yoksa Empty gosterilir`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("x")
        advanceUntilIdle()

        assertEquals(LoadState.Empty, viewModel.uiState.value)
        assertEquals(0, repository.callCount)
    }

    @Test
    fun `baglanti hatasi anlasilir mesaja cevrilir`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()

        viewModel.onQueryChange("xyzq")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Error bekleniyordu, gelen: $state", state is LoadState.Error)
        // Kullanıcıya teknik mesaj değil, insan dilinde bir cümle gösterilir.
        assertEquals("İnternet bağlantını kontrol et.", (state as LoadState.Error).message)
    }

    @Test
    fun `sunucu hatasi farkli mesaj gosterir`() = runTest {
        repository.error = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType()))
        )
        val viewModel = createViewModel()

        viewModel.onQueryChange("xyzq")
        advanceUntilIdle()

        assertEquals("Sunucu şu an cevap vermiyor.", (viewModel.uiState.value as LoadState.Error).message)
    }

    @Test
    fun `ag hatasi eslesen illeri silmez`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()

        viewModel.onQueryChange("anka")
        advanceUntilIdle()

        // Hata ekranı yerine eşleşen il (Ankara) gösterilmeye devam eder.
        assertEquals(listOf("Ankara"), viewModel.names())
    }

    @Test
    fun `tekrar dene hatadan sonra yeniden arar`() = runTest {
        repository.error = IOException("no network")
        val viewModel = createViewModel()
        viewModel.onQueryChange("berlin")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is LoadState.Error)

        // Bağlantı geri geldi.
        repository.error = null
        repository.result = listOf(berlin)
        viewModel.search()
        advanceUntilIdle()

        assertEquals(listOf("Berlin"), viewModel.names())
        assertEquals(2, repository.callCount)
    }

    @Suppress("unused")
    private fun TestScope.unused() = Unit
}
