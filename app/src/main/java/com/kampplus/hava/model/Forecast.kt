package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Bir şehrin anlık durumu + önümüzdeki saatler ve günler.
 *
 * Bugün ekranının veri modeli. Zamanlar `timezone=auto` sayesinde şehrin **yerel** saatindedir;
 * telefonun saatiyle karıştırılmamalıdır ([CurrentWeather.observedAt] şehrin "şimdi"sidir).
 *
 * @param utcOffsetSeconds Şehrin UTC'den farkı (saniye). Telefon saatiyle farkı hesaplamak için.
 */
data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val utcOffsetSeconds: Int = 0
)

/**
 * Şu anki hava. Son alanlar servis tarafından her zaman gönderilmez,
 * bu yüzden nullable'dır ve ekranda yalnızca varsa gösterilir.
 */
data class CurrentWeather(
    val temperatureC: Double,
    val weatherCode: Int,
    val observedAt: LocalDateTime,
    val apparentTemperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null,
    /** Rüzgârın estiği yön, derece cinsinden (0 = kuzey, 90 = doğu). */
    val windDirectionDegrees: Int? = null,
    val pressureHpa: Double? = null,
    val visibilityMeters: Double? = null
)

/**
 * Tek bir saatlik tahmin satırı. Saat seçilince hero kartı ve ölçüm kartları bu değerlerle
 * güncellendiği için hissedilen, rüzgâr ve nem de burada taşınır.
 */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null,
    val apparentTemperatureC: Double? = null,
    val windSpeedKmh: Double? = null,
    val humidityPercent: Int? = null
)

/** Tek bir günlük tahmin satırı: o günün en düşük ve en yüksek sıcaklığı. */
data class DailyForecast(
    val date: LocalDate,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
    /** O günün en yüksek UV indeksi (0-11+). */
    val uvIndexMax: Double? = null
)

/**
 * Şehirler listesindeki bir kartın verisi: anlık sıcaklık/durum ve bugünün en düşük-en yükseği.
 * Tam tahminden çok daha hafif bir istekle alınır.
 */
data class CitySummary(
    val temperatureC: Double,
    val weatherCode: Int,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val utcOffsetSeconds: Int
)
