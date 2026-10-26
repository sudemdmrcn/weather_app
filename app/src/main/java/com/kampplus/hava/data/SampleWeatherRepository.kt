package com.kampplus.hava.data

import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

/**
 * Tasarım örneğindeki sabit hava durumu verisi. İnternet ve API gerekmez; ekranları geliştirmek
 * ve göstermek için kullanılır. Gerçek servis ile aynı arayüzü ([WeatherRepository]) uygular,
 * seçim `gradle.properties` içindeki `weather.useSampleData` bayrağıyla yapılır
 * (bkz. [WeatherRepositoryProvider]).
 *
 * Saatler gerçek "şimdi"den türetilir, böylece saatlik şerit hep güncel görünür.
 */
class SampleWeatherRepository(
    private val now: () -> LocalDateTime = { LocalDateTime.now() }
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        delay(LATENCY_MS)
        return sampleForecast(now())
    }

    override suspend fun getSummary(latitude: Double, longitude: Double): CitySummary {
        delay(LATENCY_MS)
        // Koordinattan şehri bul; bilinmeyen koordinat için ilk örneğin özeti döner.
        val index = SAMPLE_CITIES.indexOfFirst { it.latitude == latitude }.coerceAtLeast(0)
        return SAMPLE_SUMMARIES[index]
    }

    private companion object {
        /** Ağ beklemesini taklit eder; iskelet ekranlar görünsün diye. */
        const val LATENCY_MS = 350L
    }
}

/** Örnek şehir araması: ada göre süzer, boş arama tüm örnek şehirleri döndürür. */
class SampleCityRepository : CityRepository {
    override suspend fun search(query: String): List<City> {
        delay(250L)
        val text = query.trim()
        return SAMPLE_CITIES.filter { it.name.contains(text, ignoreCase = true) }
    }
}

private fun city(id: Long, name: String, lat: Double, lon: Double, code: String, country: String) =
    City(id, name, lat, lon, code, country, name)

internal val SAMPLE_CITIES = listOf(
    DEFAULT_CITY,
    city(323786L, "Ankara", 39.91987, 32.85427, "TR", "Türkiye"),
    city(311046L, "İzmir", 38.41273, 27.13838, "TR", "Türkiye"),
    city(323777L, "Antalya", 36.90812, 30.69556, "TR", "Türkiye"),
    city(738648L, "Trabzon", 41.005, 39.72694, "TR", "Türkiye"),
    city(2950159L, "Berlin", 52.52437, 13.41053, "DE", "Almanya")
)

private const val TURKEY_OFFSET = 3 * 3600
private const val BERLIN_OFFSET = 1 * 3600

/** [SAMPLE_CITIES] ile aynı sırada: anlık sıcaklık, WMO kodu, en düşük, en yüksek, UTC farkı. */
private val SAMPLE_SUMMARIES = listOf(
    CitySummary(18.0, 2, 13.0, 21.0, TURKEY_OFFSET),
    CitySummary(12.0, 3, 6.0, 14.0, TURKEY_OFFSET),
    CitySummary(22.0, 0, 15.0, 24.0, TURKEY_OFFSET),
    CitySummary(24.0, 0, 17.0, 26.0, TURKEY_OFFSET),
    CitySummary(14.0, 61, 11.0, 16.0, TURKEY_OFFSET),
    CitySummary(9.0, 61, 6.0, 11.0, BERLIN_OFFSET)
)

// Saatlik örnekler: index 0 "Şimdi", sonrası sıradaki saatler.
private val HOUR_TEMPS = listOf(18.0, 19.0, 20.0, 20.0, 19.0, 17.0, 16.0, 15.0, 14.0)
private val HOUR_CODES = listOf(2, 2, 0, 0, 2, 3, 3, 61, 61)
private val HOUR_FEELS = listOf(17.0, 18.0, 20.0, 20.0, 18.0, 16.0, 15.0, 13.0, 12.0)
private val HOUR_WIND = listOf(14.0, 15.0, 16.0, 15.0, 13.0, 12.0, 12.0, 18.0, 20.0)
private val HOUR_HUMIDITY = listOf(62, 58, 54, 55, 60, 66, 70, 82, 86)
private val HOUR_RAIN = listOf(10, 10, 5, 5, 10, 20, 30, 70, 80)

// Günlük örnekler: bugün, Pzt, Sal, Çar, Per, Cum, Cmt (gün adları bugünden itibaren dizilir).
private val DAY_CODES = listOf(2, 0, 2, 61, 61, 3, 0)
private val DAY_MIN = listOf(13.0, 12.0, 14.0, 11.0, 10.0, 11.0, 12.0)
private val DAY_MAX = listOf(21.0, 22.0, 20.0, 16.0, 15.0, 17.0, 19.0)
private val DAY_RAIN = listOf(10, 0, 10, 80, 60, 20, 0)

/** Spec'teki örnek verilerle dolu tahmin; zamanlar [now]'a göre hesaplanır. */
internal fun sampleForecast(now: LocalDateTime): Forecast {
    val firstHour = now.truncatedTo(ChronoUnit.HOURS)
    val hourly = (0 until HOURS_COUNT).map { i ->
        // Örnek listesi bittikten sonra son değer sürer.
        fun <T> pick(list: List<T>) = list.getOrElse(i) { list.last() }
        HourlyForecast(
            time = firstHour.plusHours(i.toLong()),
            temperatureC = pick(HOUR_TEMPS),
            weatherCode = pick(HOUR_CODES),
            precipitationProbability = pick(HOUR_RAIN),
            apparentTemperatureC = pick(HOUR_FEELS),
            windSpeedKmh = pick(HOUR_WIND),
            humidityPercent = pick(HOUR_HUMIDITY)
        )
    }
    val today = now.toLocalDate()
    val daily = DAY_CODES.indices.map { i ->
        DailyForecast(
            date = today.plusDays(i.toLong()),
            minTemperatureC = DAY_MIN[i],
            maxTemperatureC = DAY_MAX[i],
            weatherCode = DAY_CODES[i],
            precipitationProbability = DAY_RAIN[i],
            sunrise = today.plusDays(i.toLong()).atTime(7, 12),
            sunset = today.plusDays(i.toLong()).atTime(18, 9),
            uvIndexMax = 3.0
        )
    }
    return Forecast(
        current = CurrentWeather(
            temperatureC = HOUR_TEMPS[0],
            weatherCode = HOUR_CODES[0],
            observedAt = now,
            apparentTemperatureC = HOUR_FEELS[0],
            humidityPercent = HOUR_HUMIDITY[0],
            windSpeedKmh = HOUR_WIND[0],
            windDirectionDegrees = 315,
            pressureHpa = 1013.0,
            visibilityMeters = 24_000.0
        ),
        hourly = hourly,
        daily = daily,
        utcOffsetSeconds = TURKEY_OFFSET
    )
}

private const val HOURS_COUNT = 30
