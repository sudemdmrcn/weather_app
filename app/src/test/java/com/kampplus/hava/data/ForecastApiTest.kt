package com.kampplus.hava.data

import com.kampplus.hava.testing.createApi
import com.kampplus.hava.testing.readResource
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tahmin yanıtının sütun biçiminden satır biçimine doğru çevrildiğini doğrular.
 *
 * Sütun → satır çevrimi (bkz. [toForecast]) bu projedeki en kolay bozulan yerdir:
 * bir index kayması hiç hata vermez, yalnızca yanlış saatte yanlış sıcaklık gösterir.
 */
class ForecastApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ForecastApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = server.createApi()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private suspend fun fetchForecast() = api.getForecast(39.93, 32.87).toForecast()

    @Test
    fun `anlik hava alanlari dogru okunur`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        val current = fetchForecast().current

        assertEquals(14.5, current.temperatureC, 0.01)
        assertEquals(2, current.weatherCode)
        assertEquals(LocalDateTime.parse("2026-09-24T11:30"), current.observedAt)
        assertEquals(13.1, checkNotNull(current.apparentTemperatureC), 0.01)
        assertEquals(48, current.humidityPercent)
        assertEquals(9.7, checkNotNull(current.windSpeedKmh), 0.01)
    }

    @Test
    fun `saatlik sutunlar ayni index ile satirlara donusur`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        val hourly = fetchForecast().hourly

        // Fixture'da 3 saat var ama index 1'in sıcaklığı null; o satır atlanır.
        assertEquals(2, hourly.size)

        assertEquals(LocalDateTime.parse("2026-09-24T00:00"), hourly[0].time)
        assertEquals(12.7, hourly[0].temperatureC, 0.01)
        assertEquals(2, hourly[0].weatherCode)
        assertEquals(0, hourly[0].precipitationProbability)

        // Atlanan satırdan sonra eşleşme kaymamalı: 02:00 -> 11.3 ve kod 61.
        assertEquals(LocalDateTime.parse("2026-09-24T02:00"), hourly[1].time)
        assertEquals(11.3, hourly[1].temperatureC, 0.01)
        assertEquals(61, hourly[1].weatherCode)
        assertEquals(35, hourly[1].precipitationProbability)
    }

    @Test
    fun `gunluk sutunlar min ve max ile eslenir`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        val daily = fetchForecast().daily

        assertEquals(2, daily.size)
        assertEquals(LocalDate.parse("2026-09-24"), daily[0].date)
        assertEquals(9.0, daily[0].minTemperatureC, 0.01)
        assertEquals(18.1, daily[0].maxTemperatureC, 0.01)
        assertEquals(LocalDate.parse("2026-09-25"), daily[1].date)
        assertEquals(6.2, daily[1].minTemperatureC, 0.01)
        assertEquals(20.1, daily[1].maxTemperatureC, 0.01)
    }

    @Test
    fun `ek olcumler anlik ve gunluk veriden okunur`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        val forecast = fetchForecast()

        assertEquals(315, forecast.current.windDirectionDegrees)
        assertEquals(1012.4, checkNotNull(forecast.current.pressureHpa), 0.01)
        assertEquals(24140.0, checkNotNull(forecast.current.visibilityMeters), 0.01)
        assertEquals(LocalDateTime.parse("2026-09-24T06:41"), forecast.daily[0].sunrise)
        assertEquals(LocalDateTime.parse("2026-09-24T18:48"), forecast.daily[0].sunset)
        assertEquals(5.4, checkNotNull(forecast.daily[0].uvIndexMax), 0.01)
        // İkinci günün değerleri birinciyle karışmamalı.
        assertEquals(4.1, checkNotNull(forecast.daily[1].uvIndexMax), 0.01)
    }

    @Test
    fun `ek olcumler gelmezse null kalir`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"latitude":39.9,"longitude":32.8,
                   "current":{"time":"2026-09-24T11:30","temperature_2m":14.5,"weather_code":2}}"""
            )
        )

        val current = api.getForecast(39.9, 32.8).toForecast().current

        assertNull(current.windDirectionDegrees)
        assertNull(current.pressureHpa)
        assertNull(current.visibilityMeters)
    }

    @Test
    fun `saatlik ek alanlar ve saat dilimi farki okunur`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        val forecast = fetchForecast()

        assertEquals(10_800, forecast.utcOffsetSeconds)
        val hourly = forecast.hourly
        assertEquals(11.5, checkNotNull(hourly[0].apparentTemperatureC), 0.01)
        assertEquals(8.0, checkNotNull(hourly[0].windSpeedKmh), 0.01)
        assertEquals(70, hourly[0].humidityPercent)
        // Atlanan satırdan sonra eşleşme kaymamalı: 02:00 -> 10.2 / 10.0 / 72.
        assertEquals(10.2, checkNotNull(hourly[1].apparentTemperatureC), 0.01)
        assertEquals(10.0, checkNotNull(hourly[1].windSpeedKmh), 0.01)
        assertEquals(72, hourly[1].humidityPercent)
    }

    @Test
    fun `ozet istegi hafif alanlarla gider ve ozete cevrilir`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"latitude":52.5,"longitude":13.4,"utc_offset_seconds":3600,
                   "current":{"time":"2026-10-25T11:30","temperature_2m":9.0,"weather_code":61},
                   "daily":{"time":["2026-10-25"],"temperature_2m_max":[11.0],"temperature_2m_min":[6.0]}}"""
            )
        )

        val summary = api.getSummary(52.5, 13.4).toSummary()

        assertEquals(9.0, summary.temperatureC, 0.01)
        assertEquals(61, summary.weatherCode)
        assertEquals(6.0, summary.minTemperatureC, 0.01)
        assertEquals(11.0, summary.maxTemperatureC, 0.01)
        assertEquals(3_600, summary.utcOffsetSeconds)

        val path = checkNotNull(server.takeRequest().path)
        assertTrue("Tek günlük istenmeli: $path", path.contains("forecast_days=1"))
        assertTrue("Saatlik veri istenmemeli: $path", !path.contains("hourly="))
    }

    @Test
    fun `ozet yanitinda gunluk blok yoksa hata firlatilir`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"current":{"time":"2026-10-25T11:30","temperature_2m":9.0,"weather_code":61}}"""
            )
        )

        val response = api.getSummary(52.5, 13.4)

        assertThrows(IllegalStateException::class.java) { response.toSummary() }
    }

    @Test
    fun `current blogu yoksa hata firlatilir`() = runTest {
        // Bozuk yanıt: sadece koordinatlar var. ViewModel bunu Error durumuna çevirir.
        server.enqueue(MockResponse().setBody("""{"latitude":39.9,"longitude":32.8}"""))

        val response = api.getForecast(39.9, 32.8)

        assertThrows(IllegalStateException::class.java) { response.toForecast() }
    }

    @Test
    fun `saatlik ve gunluk bloklar yoksa bos liste doner`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"latitude":39.9,"longitude":32.8,
                   "current":{"time":"2026-09-24T11:30","temperature_2m":14.5,"weather_code":2}}"""
            )
        )

        val forecast = api.getForecast(39.9, 32.8).toForecast()

        assertTrue(forecast.hourly.isEmpty())
        assertTrue(forecast.daily.isEmpty())
    }

    @Test
    fun `istek dogru yola ve parametrelerle gider`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("forecast_single.json")))

        api.getForecast(39.93, 32.87)

        val path = checkNotNull(server.takeRequest().path)
        assertTrue("Yol v1/forecast olmali: $path", path.startsWith("/v1/forecast"))
        assertTrue("latitude eksik: $path", path.contains("latitude=39.93"))
        assertTrue("longitude eksik: $path", path.contains("longitude=32.87"))
        // timezone=auto olmazsa zamanlar UTC gelir ve saatler kayar.
        assertTrue("timezone=auto olmali: $path", path.contains("timezone=auto"))
        // İstenmeyen alan yanıtta hiç gelmez; yeni ölçümler istek listesinde olmalı.
        listOf("wind_direction_10m", "surface_pressure", "visibility", "sunrise", "sunset", "uv_index_max")
            .forEach { field -> assertTrue("$field istenmeli: $path", path.contains(field)) }
    }
}
