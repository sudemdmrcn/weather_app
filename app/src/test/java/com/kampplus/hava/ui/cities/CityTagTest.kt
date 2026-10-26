package com.kampplus.hava.ui.cities

import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Şehir kartının küçük etiketi: "Konumum" ya da "Ülke · N saat geride". */
class CityTagTest {

    private val phoneOffset = 10_800 // UTC+3
    private val berlin = City(2950159L, "Berlin", 52.5, 13.4, "DE", "Almanya", "Berlin")
    private val ankara = City(323786L, "Ankara", 39.9, 32.8, "TR", "Türkiye", "Ankara")

    private fun summary(offset: Int) = CitySummary(10.0, 2, 5.0, 12.0, offset)

    @Test
    fun `ilk sehir konumum etiketi alir`() {
        val card = CityCardData(DEFAULT_CITY, summary(10_800), isHome = true)

        assertEquals("Konumum", cityTag(card, "TR", phoneOffset, "Konumum"))
    }

    @Test
    fun `yabanci sehirde ulke ve saat farki yazilir`() {
        val card = CityCardData(berlin, summary(3_600), isHome = false)

        assertEquals("Almanya · 2 saat geride", cityTag(card, "TR", phoneOffset, "Konumum"))
    }

    @Test
    fun `ayni ulke ve ayni saat diliminde etiket yoktur`() {
        val card = CityCardData(ankara, summary(10_800), isHome = false)

        assertNull(cityTag(card, "TR", phoneOffset, "Konumum"))
    }

    @Test
    fun `ozeti olmayan yabanci sehirde yalnizca ulke yazilir`() {
        val card = CityCardData(berlin, summary = null, isHome = false)

        assertEquals("Almanya", cityTag(card, "TR", phoneOffset, "Konumum"))
    }
}
