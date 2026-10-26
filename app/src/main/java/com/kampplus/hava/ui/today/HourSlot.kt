package com.kampplus.hava.ui.today

import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Saatlik şeritteki bir hap ve o hap seçilince hero/ölçüm kartlarında görünecek değerler.
 *
 * @property time Saatin yerel zamanı. "Şimdi" için içinde bulunulan saattir.
 * @property isNow İlk hap "Şimdi"dir; değerleri anlık ölçümden gelir.
 */
data class HourSlot(
    val time: LocalDateTime,
    val isNow: Boolean,
    val temperatureC: Double,
    val weatherCode: Int,
    val apparentTemperatureC: Double?,
    val windSpeedKmh: Double?,
    val humidityPercent: Int?,
    val precipitationProbability: Int?
)

/**
 * Tahminden saat hapları üretir: ilk hap "Şimdi", sonrası ardışık saatler (toplam en çok [count]).
 *
 * Önemli: "şimdi" **şehrin** saatidir (`current.observedAt`), telefonun saati değil. Aksi hâlde
 * Berlin'e bakan İstanbullu kullanıcı yanlış saatleri görürdü.
 *
 * "Şimdi" hapının hissedilen/rüzgâr/nem değerleri anlık ölçümden alınır, anlık ölçümde yoksa o
 * saatin tahmininden tamamlanır. Yağış olasılığı yalnızca saatlik veride bulunur.
 */
fun buildHourSlots(forecast: Forecast, count: Int = 24): List<HourSlot> {
    val current = forecast.current
    val nowHour = current.observedAt.truncatedTo(ChronoUnit.HOURS)
    val thisHour = forecast.hourly.firstOrNull { it.time == nowHour }
    val now = HourSlot(
        time = nowHour,
        isNow = true,
        temperatureC = current.temperatureC,
        weatherCode = current.weatherCode,
        apparentTemperatureC = current.apparentTemperatureC ?: thisHour?.apparentTemperatureC,
        windSpeedKmh = current.windSpeedKmh ?: thisHour?.windSpeedKmh,
        humidityPercent = current.humidityPercent ?: thisHour?.humidityPercent,
        precipitationProbability = thisHour?.precipitationProbability
    )
    val upcoming = forecast.hourly
        .filter { it.time.isAfter(nowHour) }
        .take((count - 1).coerceAtLeast(0))
        .map { it.toSlot() }
    return listOf(now) + upcoming
}

private fun HourlyForecast.toSlot() = HourSlot(
    time = time,
    isNow = false,
    temperatureC = temperatureC,
    weatherCode = weatherCode,
    apparentTemperatureC = apparentTemperatureC,
    windSpeedKmh = windSpeedKmh,
    humidityPercent = humidityPercent,
    precipitationProbability = precipitationProbability
)
