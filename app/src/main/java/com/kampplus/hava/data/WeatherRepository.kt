package com.kampplus.hava.data

import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.Forecast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hava tahmini verisine erişimin tek kapısı.
 *
 * [CityRepository] ile aynı desen: arayüz + gerçek uygulama. Ekranlar yalnızca
 * bu arayüzü tanır; Retrofit'i veya JSON'u bilmez. Gerçek servis yerine örnek veri
 * kullanmak için bkz. [WeatherRepositoryProvider].
 */
interface WeatherRepository {
    /** Verilen koordinat için anlık durum + saatlik + günlük tahmini getirir. */
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast

    /** Şehirler listesindeki kart için hafif özet: anlık sıcaklık/durum ve bugünün en düşük-en yükseği. */
    suspend fun getSummary(latitude: Double, longitude: Double): CitySummary
}

/** Gerçek uygulama: tahmini Open-Meteo forecast servisinden alır. */
class OpenMeteoWeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast = withContext(ioDispatcher) {
        // Ağ yanıtı sütun biçimindedir; toForecast() onu ekranın beklediği
        // satır biçimine çevirir (bkz. ForecastResponse.kt).
        api.getForecast(latitude = latitude, longitude = longitude).toForecast()
    }

    override suspend fun getSummary(latitude: Double, longitude: Double): CitySummary = withContext(ioDispatcher) {
        api.getSummary(latitude = latitude, longitude = longitude).toSummary()
    }
}
