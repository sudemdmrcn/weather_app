package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Test

/** °C → °F dönüşümü ve yuvarlama; birim değişiminde ekrandaki her sayı buna dayanır. */
class TemperatureUnitTest {

    @Test
    fun `celsius degeri en yakin tam sayiya yuvarlanir`() {
        assertEquals(18, TemperatureUnit.CELSIUS.convert(17.6))
        assertEquals(17, TemperatureUnit.CELSIUS.convert(17.4))
        assertEquals(-3, TemperatureUnit.CELSIUS.convert(-3.2))
    }

    @Test
    fun `fahrenheit donusumu formule uyar`() {
        assertEquals(32, TemperatureUnit.FAHRENHEIT.convert(0.0))
        assertEquals(212, TemperatureUnit.FAHRENHEIT.convert(100.0))
        assertEquals(64, TemperatureUnit.FAHRENHEIT.convert(18.0)) // 64,4
        assertEquals(70, TemperatureUnit.FAHRENHEIT.convert(21.0)) // 69,8
    }

    @Test
    fun `eksi sicaklik fahrenheit olarak doner`() {
        assertEquals(27, TemperatureUnit.FAHRENHEIT.convert(-3.0)) // 26,6
        assertEquals(-4, TemperatureUnit.FAHRENHEIT.convert(-20.0))
    }

    @Test
    fun `format derece isaretiyle yazar`() {
        assertEquals("18°", TemperatureUnit.CELSIUS.format(18.2))
        assertEquals("65°", TemperatureUnit.FAHRENHEIT.format(18.2)) // 64,76
    }
}
