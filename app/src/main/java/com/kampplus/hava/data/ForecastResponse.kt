package com.kampplus.hava.data

import com.google.gson.annotations.SerializedName
import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Open-Meteo `/v1/forecast` yanıtının ham hali.
 *
 * Neden ayrı bir model? Şehir aramasında JSON doğrudan ekranda kullandığımız biçimdeydi
 * (bkz. [com.kampplus.hava.model.City]). Tahminde değil: servis veriyi **sütun** biçiminde
 * gönderir. Yani "her saat için bir nesne" yerine, her alan ayrı bir listedir:
 *
 * ```
 * "hourly": {
 *   "time":           ["2026-01-15T00:00", "2026-01-15T01:00", ...],
 *   "temperature_2m": [12.4,               11.8,               ...]
 * }
 * ```
 *
 * Aynı index aynı saate aittir. [toForecast] bu sütunları satırlara çevirir.
 */
data class ForecastResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int = 0,
    val current: CurrentResponse? = null,
    val hourly: HourlyResponse? = null,
    val daily: DailyResponse? = null
)

data class CurrentResponse(
    val time: String,
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double? = null,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: Int? = null,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double? = null,
    @SerializedName("wind_direction_10m")
    val windDirection: Int? = null,
    @SerializedName("surface_pressure")
    val pressure: Double? = null,
    val visibility: Double? = null
)

data class HourlyResponse(
    val time: List<String>? = null,
    @SerializedName("temperature_2m")
    val temperature: List<Double?>? = null,
    @SerializedName("weather_code")
    val weatherCode: List<Int?>? = null,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int?>? = null,
    @SerializedName("apparent_temperature")
    val apparentTemperature: List<Double?>? = null,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double?>? = null,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: List<Int?>? = null
)

data class DailyResponse(
    val time: List<String>? = null,
    @SerializedName("weather_code")
    val weatherCode: List<Int?>? = null,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double?>? = null,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double?>? = null,
    @SerializedName("precipitation_probability_max")
    val precipitationProbabilityMax: List<Int?>? = null,
    val sunrise: List<String?>? = null,
    val sunset: List<String?>? = null,
    @SerializedName("uv_index_max")
    val uvIndexMax: List<Double?>? = null
)

/**
 * Ağ modelini ekranın kullandığı [Forecast] modeline çevirir.
 *
 * `current` bloğu istenmiş ama gelmemişse yanıt bozuktur; hata fırlatırız ve
 * ViewModel bunu "Error" durumuna çevirir.
 */
fun ForecastResponse.toForecast(): Forecast {
    val currentResponse = current
        ?: throw IllegalStateException("Forecast response has no current block")
    return Forecast(
        current = currentResponse.toCurrentWeather(),
        hourly = hourly.toHourlyForecasts(),
        daily = daily.toDailyForecasts(),
        utcOffsetSeconds = utcOffsetSeconds
    )
}

/**
 * Hafif "özet" yanıtını şehir kartının kullandığı [CitySummary]'ye çevirir.
 *
 * Özet isteğinde `current` ve `daily` (tek günlük) bulunur; ikisinden biri eksikse kart
 * çizilemez, bu yüzden hata fırlatılır ve çağıran taraf o şehri "veri yok" olarak gösterir.
 */
fun ForecastResponse.toSummary(): CitySummary {
    val currentResponse = current
        ?: throw IllegalStateException("Summary response has no current block")
    // Özet isteği günlük weather_code istemez; bu yüzden toDailyForecasts() yerine doğrudan okunur.
    val min = daily?.temperatureMin?.firstOrNull()
        ?: throw IllegalStateException("Summary response has no daily minimum")
    val max = daily.temperatureMax?.firstOrNull()
        ?: throw IllegalStateException("Summary response has no daily maximum")
    return CitySummary(
        temperatureC = currentResponse.temperature,
        weatherCode = currentResponse.weatherCode,
        minTemperatureC = min,
        maxTemperatureC = max,
        utcOffsetSeconds = utcOffsetSeconds
    )
}

/** `timezone=auto` ile zamanlar ofsetsiz ISO formatında gelir: "2026-01-15T14:00". */
private fun CurrentResponse.toCurrentWeather() = CurrentWeather(
    temperatureC = temperature,
    weatherCode = weatherCode,
    observedAt = LocalDateTime.parse(time),
    apparentTemperatureC = apparentTemperature,
    humidityPercent = relativeHumidity,
    windSpeedKmh = windSpeed,
    windDirectionDegrees = windDirection,
    pressureHpa = pressure,
    visibilityMeters = visibility
)

/**
 * Sütun → satır çevrimi. Zaman listesinin her index'i için diğer listelerden
 * aynı index'teki değerler okunur.
 *
 * `mapNotNull` + `return@mapNotNull null`: bir satırın zorunlu alanı eksikse o satır
 * atlanır. Böylece tek bir bozuk kayıt yüzünden bütün ekran çökmez.
 */
private fun HourlyResponse?.toHourlyForecasts(): List<HourlyForecast> {
    val times = this?.time ?: return emptyList()
    return times.indices.mapNotNull { index ->
        val temperature = temperature?.getOrNull(index) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(index) ?: return@mapNotNull null
        HourlyForecast(
            time = LocalDateTime.parse(times[index]),
            temperatureC = temperature,
            weatherCode = code,
            precipitationProbability = precipitationProbability?.getOrNull(index),
            apparentTemperatureC = apparentTemperature?.getOrNull(index),
            windSpeedKmh = windSpeed?.getOrNull(index),
            humidityPercent = relativeHumidity?.getOrNull(index)
        )
    }
}

/** Günlük veri de sütun biçimindedir; tek farkı zamanın saat değil tarih olması. */
private fun DailyResponse?.toDailyForecasts(): List<DailyForecast> {
    val times = this?.time ?: return emptyList()
    return times.indices.mapNotNull { index ->
        val max = temperatureMax?.getOrNull(index) ?: return@mapNotNull null
        val min = temperatureMin?.getOrNull(index) ?: return@mapNotNull null
        val code = weatherCode?.getOrNull(index) ?: return@mapNotNull null
        DailyForecast(
            date = LocalDate.parse(times[index]),
            minTemperatureC = min,
            maxTemperatureC = max,
            weatherCode = code,
            precipitationProbability = precipitationProbabilityMax?.getOrNull(index),
            sunrise = sunrise?.getOrNull(index)?.let(LocalDateTime::parse),
            sunset = sunset?.getOrNull(index)?.let(LocalDateTime::parse),
            uvIndexMax = uvIndexMax?.getOrNull(index)
        )
    }
}
