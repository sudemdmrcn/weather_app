package com.kampplus.hava.ui.today

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Ölçüm değerlerinin Türkçe metne çevrimini korur. */
class WeatherTextTest {

    @Test
    fun `rüzgar yonu pusula adina cevrilir`() {
        assertEquals("Kuzey", windDirectionLabel(0))
        assertEquals("Doğu", windDirectionLabel(90))
        assertEquals("Güney", windDirectionLabel(180))
        assertEquals("Batı", windDirectionLabel(270))
        assertEquals("Güneybatı", windDirectionLabel(225))
    }

    @Test
    fun `kuzey iki taraftan da kuzey sayilir`() {
        // 350° ve 10°, 0°'nin iki yanındadır; ikisi de kuzeydir.
        assertEquals("Kuzey", windDirectionLabel(350))
        assertEquals("Kuzey", windDirectionLabel(10))
        assertEquals("Kuzey", windDirectionLabel(360))
    }

    @Test
    fun `dilim sinirlari dogru tarafa duser`() {
        assertEquals("Kuzey", windDirectionLabel(22))
        assertEquals("Kuzeydoğu", windDirectionLabel(23))
    }

    @Test
    fun `uv indeksi risk kademesine cevrilir`() {
        assertEquals("Düşük", uvLevelLabel(0.0))
        assertEquals("Orta", uvLevelLabel(3.0))
        assertEquals("Yüksek", uvLevelLabel(7.9))
        assertEquals("Çok yüksek", uvLevelLabel(8.0))
        assertEquals("Aşırı", uvLevelLabel(11.0))
    }

    @Test
    fun `gorus mesafesi kilometreye cevrilir`() {
        assertEquals("4,5 km", visibilityLabel(4500.0))
        assertEquals("24 km", visibilityLabel(24140.0))
    }

    @Test
    fun `gun adlari kisa turkce yazilir`() {
        assertEquals("Pzt", dayShortLabel(LocalDate.parse("2026-10-26")))
        assertEquals("Çar", dayShortLabel(LocalDate.parse("2026-10-28")))
        assertEquals("Paz", dayShortLabel(LocalDate.parse("2026-10-25")))
    }

    @Test
    fun `tarih turkce ve uzun yazilir`() {
        assertEquals("Pazar, 25 Ekim", dateLabel(LocalDate.parse("2026-10-25")))
    }

    @Test
    fun `saat farki geride ve ileride olarak yazilir`() {
        // Berlin UTC+1, telefon UTC+3 -> 2 saat geride.
        assertEquals("2 saat geride", timeDifferenceLabel(3600, 10800))
        assertEquals("1 saat ileride", timeDifferenceLabel(10800, 7200))
    }

    @Test
    fun `ayni saat diliminde etiket yoktur`() {
        assertNull(timeDifferenceLabel(10800, 10800))
    }

    @Test
    fun `yarim saatlik fark ondalikla yazilir`() {
        // Hindistan UTC+5:30, telefon UTC+3 -> 2,5 saat ileride.
        assertEquals("2,5 saat ileride", timeDifferenceLabel(19800, 10800))
    }
}
