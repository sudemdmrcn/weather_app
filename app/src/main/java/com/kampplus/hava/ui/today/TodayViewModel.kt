package com.kampplus.hava.ui.today

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
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
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

/** Bugün ekranının gösterdiği şehir ve tahmini. [isHome] ilk kayıtlı şehir ("Konumum") içindir. */
data class TodayContent(val city: City, val isHome: Boolean, val forecast: Forecast)

/**
 * Bugün ekranının beyni.
 *
 * Seçili şehir tercihlerden ([PreferencesRepository.selectedCity]) gelir: Şehirler sekmesinde
 * başka bir şehre dokunulunca bu ViewModel yeni şehri kendiliğinden yükler. Yükleme yalnızca
 * ekran dinlenirken yapılır (`WhileSubscribed`); ekran uzun süre arka plandaysa durur,
 * dönüldüğünde taze veri çekilir.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val preferences: PreferencesRepository,
    private val weather: WeatherRepository
) : ViewModel() {

    private val reload = MutableStateFlow(0)

    private val _selectedHour = MutableStateFlow(0)

    /** Saatlik şeritte seçili hapın sırası; 0 = "Şimdi". */
    val selectedHour: StateFlow<Int> = _selectedHour.asStateFlow()

    /** Gösterim birimi; değişimi kalıcıdır. */
    val unit: StateFlow<TemperatureUnit> = preferences.unit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TemperatureUnit.CELSIUS)

    val uiState: StateFlow<LoadState<TodayContent>> =
        combine(preferences.selectedCity, preferences.savedCities, reload) { city, saved, attempt ->
            Triple(city, saved.first().id == city.id, attempt)
        }
            // Birim değişimi gibi alakasız tercih yazımları aynı değerleri tekrar yayar; yeniden yükleme olmasın.
            .distinctUntilChanged()
            .transformLatest { (city, isHome, _) ->
                _selectedHour.value = 0
                emit(LoadState.Loading)
                emitForecast(city, isHome)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LoadState.Loading)

    private suspend fun FlowCollector<LoadState<TodayContent>>.emitForecast(city: City, isHome: Boolean) {
        emit(
            try {
                LoadState.Success(TodayContent(city, isHome, weather.getForecast(city.latitude, city.longitude)))
            } catch (e: IOException) {
                Log.e(TAG, "Tahmin alınamadı: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Tahmin alınamadı: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Tahmin alınamadı: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            } catch (e: IllegalStateException) {
                // toForecast(), "current" bloğu yoksa bunu fırlatır.
                Log.e(TAG, "Tahmin alınamadı: eksik veri", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        )
    }

    fun onHourSelected(index: Int) {
        _selectedHour.value = index
    }

    fun onUnitChange(unit: TemperatureUnit) {
        viewModelScope.launch { preferences.setUnit(unit) }
    }

    /** Hata ekranındaki "Tekrar dene" düğmesi. */
    fun onRetry() {
        reload.value += 1
    }

    companion object {
        private const val TAG = "TodayViewModel"
        private const val STOP_TIMEOUT_MS = 5 * 60_000L

        /** `viewModel(factory = TodayViewModel.Factory)`: DataStore için Application gerekir. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodayViewModel(
                    preferences = DataStorePreferencesRepository(checkNotNull(this[APPLICATION_KEY])),
                    weather = WeatherRepositoryProvider.weather
                )
            }
        }
    }
}
