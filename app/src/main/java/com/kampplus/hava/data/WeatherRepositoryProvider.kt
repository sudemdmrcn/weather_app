package com.kampplus.hava.data

import com.kampplus.hava.BuildConfig

/**
 * Hangi veri kaynağının kullanılacağını **tek yerden** seçer: gerçek Open-Meteo servisi ya da
 * tasarımdaki örnek veri ([SampleWeatherRepository]).
 *
 * Seçim derleme zamanında `gradle.properties` içindeki bayrakla yapılır:
 * ```
 * weather.useSampleData=true   # örnek veri, internet gerekmez
 * weather.useSampleData=false  # gerçek Open-Meteo (varsayılan)
 * ```
 * Ekranlar ve ViewModel'ler yalnızca [WeatherRepository] / [CityRepository] arayüzünü bilir.
 */
object WeatherRepositoryProvider {

    val weather: WeatherRepository by lazy {
        if (BuildConfig.USE_SAMPLE_DATA) SampleWeatherRepository() else OpenMeteoWeatherRepository()
    }

    val cities: CityRepository by lazy {
        if (BuildConfig.USE_SAMPLE_DATA) SampleCityRepository() else OpenMeteoCityRepository()
    }
}
