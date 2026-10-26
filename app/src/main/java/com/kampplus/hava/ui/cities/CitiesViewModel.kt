package com.kampplus.hava.ui.cities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.DataStorePreferencesRepository
import com.kampplus.hava.data.PreferencesRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.data.WeatherRepositoryProvider
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehirler listesindeki bir kartın verisi.
 *
 * @property summary Şehrin anlık özeti. `null` ise o şehrin verisi alınamamıştır; kart yine
 *   çizilir ("—"), böylece tek şehrin hatası listenin tamamını bozmaz.
 * @property isHome İlk kayıtlı şehir; "Konumum" olarak işaretlenir.
 */
data class CityCardData(val city: City, val summary: CitySummary?, val isHome: Boolean)

/**
 * Şehirler ekranının beyni: kayıtlı şehirleri ve her birinin hafif özetini yükler, arama kutusuna
 * göre süzer. Şehre dokunma ve şehir ekleme de burada tercihlere yazılır.
 *
 * Yükleme yalnızca [uiState] dinlenirken yapılır; "şehir ekle" akışı bu ViewModel'i yalnızca
 * [addAndSelect] için kullandığından gereksiz ağ isteği atılmaz.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CitiesViewModel(
    private val preferences: PreferencesRepository,
    private val weather: WeatherRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)
    private val _query = MutableStateFlow("")

    /** Arama kutusundaki metin; yalnızca kayıtlı şehirleri süzer, internete gitmez. */
    val query: StateFlow<String> = _query.asStateFlow()

    val unit: StateFlow<TemperatureUnit> = preferences.unit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TemperatureUnit.CELSIUS)

    private val loaded: Flow<LoadState<List<CityCardData>>> =
        combine(preferences.savedCities.distinctUntilChanged(), reload) { cities, _ -> cities }
            .transformLatest { cities ->
                emit(LoadState.Loading)
                emit(loadCards(cities))
            }

    val uiState: StateFlow<LoadState<List<CityCardData>>> = combine(loaded, _query) { state, query ->
        if (state is LoadState.Success) {
            val filtered = state.data.filter { it.city.name.contains(query.trim(), ignoreCase = true) }
            if (filtered.isEmpty()) LoadState.Empty else LoadState.Success(filtered)
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LoadState.Loading)

    /** Tüm şehirlerin özetini paralel ister; hepsi başarısızsa hata, bir kısmı başarısızsa "—" gösterir. */
    private suspend fun loadCards(cities: List<City>): LoadState<List<CityCardData>> {
        val summaries = coroutineScope {
            cities.map { city -> async { fetchSummary(city) } }.awaitAll()
        }
        if (cities.isNotEmpty() && summaries.all { it == null }) {
            return LoadState.Error("İnternet bağlantını kontrol et.")
        }
        return LoadState.Success(cities.mapIndexed { index, city -> CityCardData(city, summaries[index], isHome = index == 0) })
    }

    private suspend fun fetchSummary(city: City): CitySummary? = try {
        weather.getSummary(city.latitude, city.longitude)
    } catch (e: IOException) {
        Log.e(TAG, "${city.name} özeti alınamadı: bağlantı hatası", e)
        null
    } catch (e: HttpException) {
        Log.e(TAG, "${city.name} özeti alınamadı: HTTP ${e.code()}", e)
        null
    } catch (e: JsonSyntaxException) {
        Log.e(TAG, "${city.name} özeti alınamadı: yanıt çözümlenemedi", e)
        null
    } catch (e: IllegalStateException) {
        Log.e(TAG, "${city.name} özeti alınamadı: eksik veri", e)
        null
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onRetry() {
        reload.value += 1
    }

    /** Karta dokunulduğunda: şehri Bugün ekranında gösterilecek şehir yapar. */
    fun onCityClick(city: City) {
        viewModelScope.launch { preferences.selectCity(city.id) }
    }

    /**
     * Arama sonucundan bir şehri kaydeder ve seçer. Çağıran, işin bittiğini bilsin diye `suspend`tir;
     * ekran geçişi bu çağrı döndükten sonra yapılmalıdır (yoksa ViewModel kapanınca yazma yarıda kalır).
     */
    suspend fun addAndSelect(city: City) {
        preferences.addCity(city)
        preferences.selectCity(city.id)
    }

    companion object {
        private const val TAG = "CitiesViewModel"
        private const val STOP_TIMEOUT_MS = 5 * 60_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CitiesViewModel(
                    preferences = DataStorePreferencesRepository(checkNotNull(this[APPLICATION_KEY])),
                    weather = WeatherRepositoryProvider.weather
                )
            }
        }
    }
}
