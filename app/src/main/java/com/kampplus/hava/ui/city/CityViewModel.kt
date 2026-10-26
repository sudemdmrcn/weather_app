package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.TurkishProvinces
import com.kampplus.hava.data.WeatherRepositoryProvider
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehir arama ekranının beyni. Harf yazdıkça sonuçlar değişir, Enter'a basmak gerekmez.
 *
 * İki kaynak birleşir:
 * 1. **Yerel il listesi** ([TurkishProvinces]): ada göre **başlayan** iller, her harfte anında
 *    süzülür, internet gerekmez. Boş arama 81 ilin hepsini gösterir. İller doğru merkez
 *    koordinatlarıyla gelir (Kocaeli → İzmit).
 * 2. **Open-Meteo araması**: dünya genelindeki diğer şehirler. Servis tek harfe sonuç vermediği
 *    için en az [MIN_REMOTE_CHARS] harf gerekir; kullanıcı yazmayı bıraktıktan [debounceMs] sonra
 *    istenir ki her tuşa basışta ağ isteği atılmasın.
 *
 * Yerel sonuçlar hemen görünür, ağ sonuçları gelince listenin devamına eklenir. Ağ başarısız olsa
 * bile eşleşen il varsa liste kaybolmaz.
 *
 * @param repository Varsayılanı gerçek servis (ya da örnek veri). Testlerde sahte verilir.
 * @param provinces Yerel il listesi.
 * @param debounceMs Son harften sonra ağ isteğinden önce beklenen süre.
 */
class CityViewModel(
    private val repository: CityRepository = WeatherRepositoryProvider.cities,
    private val provinces: List<City> = TurkishProvinces.all,
    private val debounceMs: Long = DEBOUNCE_MS
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // Açılışta arama kutusu boştur ve 81 ilin tamamı hazırdır; ağ beklenmez.
    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Success(provinces))
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** Kullanıcı arama kutusuna her harf yazdığında ya da sildiğinde çağrılır. */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        runSearch(waitForTyping = true)
    }

    /** Klavyedeki arama tuşu ve "Tekrar dene": beklemeden hemen arar. */
    fun search() = runSearch(waitForTyping = false)

    /**
     * Aramayı çalıştırır. Önceki arama hâlâ sürüyorsa iptal edilir; böylece hızlı yazarken yalnızca
     * son metnin sonucu ekrana gelir (eski bir cevap yenisinin üstüne yazamaz).
     */
    private fun runSearch(waitForTyping: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val text = _query.value.trim()
            val local = TurkishProvinces.startingWith(text, provinces)

            if (text.length < MIN_REMOTE_CHARS) {
                _uiState.value = if (local.isEmpty()) LoadState.Empty else LoadState.Success(local)
                return@launch
            }

            // Yerel eşleşmeler hemen görünür; hiç yoksa ağ cevabı beklenirken yükleniyor gösterilir.
            _uiState.value = if (local.isEmpty()) LoadState.Loading else LoadState.Success(local)
            if (waitForTyping) delay(debounceMs)

            _uiState.value = try {
                val merged = merge(local, repository.search(text))
                if (merged.isEmpty()) LoadState.Empty else LoadState.Success(merged)
            } catch (e: IOException) {
                // İnternet yok, DNS çözülemedi veya zaman aşımı.
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                fallback(local, "İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                // Sunucu yanıt verdi ama 4xx/5xx döndü.
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                fallback(local, "Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                // Beklenmeyen JSON: model ile yanıt uyuşmuyor.
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                fallback(local, "Bir şeyler ters gitti. Tekrar dene.")
                // Kullanıcıya sade mesaj, Logcat'e teknik ayrıntı.
            }
        }
    }

    /** Ağ hatasında eşleşen il varsa onu göstermeye devam eder; yoksa hata mesajı gösterir. */
    private fun fallback(local: List<City>, message: String): LoadState<List<City>> =
        if (local.isEmpty()) LoadState.Error(message) else LoadState.Success(local)

    /**
     * İller önce, ağdan gelenler sonra. Ağ sonuçlarından iki tür elenir:
     * aynı kayıt (`id` aynı) ve il adını taşıyan Türkiye'deki küçük yerler (örneğin Balıkesir'deki
     * "Kocaeli" köyü); ilin kendisi zaten doğru koordinatla en üstte durur.
     */
    private fun merge(local: List<City>, remote: List<City>): List<City> {
        val extras = remote.filter { place ->
            local.none { province ->
                province.id == place.id ||
                    (place.countryCode == "TR" && TurkishProvinces.fold(province.name) == TurkishProvinces.fold(place.name))
            }
        }
        return local + extras
    }

    companion object {
        private const val TAG = "CityViewModel"

        /** Open-Meteo tek harfe sonuç döndürmez; ağ araması en az bu kadar harfle yapılır. */
        const val MIN_REMOTE_CHARS = 2

        /** Son harften sonra ağ isteği atmadan önce beklenen süre (ms). */
        const val DEBOUNCE_MS = 300L
    }
}
