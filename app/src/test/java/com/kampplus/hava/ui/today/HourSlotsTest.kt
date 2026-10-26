package com.kampplus.hava.ui.today

import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Saat hapları: "Şimdi" şehrin saatine göre belirlenmeli, geçmiş saatler elenmeli. */
class HourSlotsTest {

    private fun hour(time: String, temp: Double, code: Int = 2, rain: Int? = null) = HourlyForecast(
        time = LocalDateTime.parse(time),
        temperatureC = temp,
        weatherCode = code,
        precipitationProbability = rain,
        apparentTemperatureC = temp - 1,
        windSpeedKmh = 10.0,
        humidityPercent = 50
    )

    private fun forecast(current: CurrentWeather, hourly: List<HourlyForecast>) =
        Forecast(current = current, hourly = hourly, daily = emptyList())

    private val hourly = listOf(
        hour("2026-10-25T12:00", 17.0),
        hour("2026-10-25T13:00", 18.0, rain = 10),
        hour("2026-10-25T14:00", 19.0),
        hour("2026-10-25T15:00", 20.0),
        hour("2026-10-25T16:00", 20.5)
    )

    private val current = CurrentWeather(
        temperatureC = 18.4,
        weatherCode = 2,
        observedAt = LocalDateTime.parse("2026-10-25T13:30"),
        apparentTemperatureC = 17.0,
        humidityPercent = 62,
        windSpeedKmh = 14.0
    )

    @Test
    fun `ilk hap simdi ve anlik olcumden gelir`() {
        val slots = buildHourSlots(forecast(current, hourly))

        val now = slots.first()
        assertTrue(now.isNow)
        assertEquals(LocalDateTime.parse("2026-10-25T13:00"), now.time)
        assertEquals(18.4, now.temperatureC, 0.01)
        assertEquals(17.0, checkNotNull(now.apparentTemperatureC), 0.01)
        assertEquals(62, now.humidityPercent)
        // Yağış olasılığı anlık ölçümde yok, o saatin tahmininden gelir.
        assertEquals(10, now.precipitationProbability)
    }

    @Test
    fun `gecmis saatler elenir ve sonraki saatler sirayla gelir`() {
        val slots = buildHourSlots(forecast(current, hourly))

        // 12:00 geçmişte, 13:00 zaten "Şimdi" hapı: kalanlar 14:00, 15:00, 16:00.
        assertEquals(
            listOf("13:00", "14:00", "15:00", "16:00"),
            slots.map { it.time.toLocalTime().toString() }
        )
        assertFalse(slots[1].isNow)
        assertEquals(19.0, slots[1].temperatureC, 0.01)
    }

    @Test
    fun `anlik olcumde olmayan degerler saatlik tahminden tamamlanir`() {
        val bare = current.copy(apparentTemperatureC = null, humidityPercent = null, windSpeedKmh = null)

        val now = buildHourSlots(forecast(bare, hourly)).first()

        assertEquals(17.0, checkNotNull(now.apparentTemperatureC), 0.01) // 18 - 1
        assertEquals(50, now.humidityPercent)
        assertEquals(10.0, checkNotNull(now.windSpeedKmh), 0.01)
    }

    @Test
    fun `saatlik veri yoksa yalnizca simdi hapi olur`() {
        val slots = buildHourSlots(forecast(current, emptyList()))

        assertEquals(1, slots.size)
        assertNull(slots.first().precipitationProbability)
    }

    @Test
    fun `hap sayisi sinirlanir`() {
        val many = (0 until 48).map {
            hour("2026-10-25T13:00", 10.0 + it).copy(time = LocalDateTime.parse("2026-10-25T13:00").plusHours(it.toLong()))
        }

        assertEquals(24, buildHourSlots(forecast(current, many)).size)
        assertEquals(5, buildHourSlots(forecast(current, many), count = 5).size)
    }

    @Test
    fun `simdi telefonun degil sehrin saatine gore bulunur`() {
        // Gözlem saati gece yarısına yakın bir şehirde "şimdi" 23:00 hapıdır, telefon saati önemsizdir.
        val night = current.copy(observedAt = LocalDateTime.parse("2026-10-25T23:40"))
        val nightHourly = listOf(
            hour("2026-10-25T23:00", 9.0),
            hour("2026-10-26T00:00", 8.0)
        )

        val slots = buildHourSlots(forecast(night, nightHourly))

        assertEquals(LocalDateTime.parse("2026-10-25T23:00"), slots[0].time)
        assertEquals(LocalDateTime.parse("2026-10-26T00:00"), slots[1].time)
    }
}
