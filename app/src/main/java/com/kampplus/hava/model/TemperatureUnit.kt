package com.kampplus.hava.model

import kotlin.math.roundToInt

/**
 * Sıcaklığın gösterileceği birim. Veri her zaman Celsius gelir ve Celsius saklanır;
 * dönüşüm yalnızca gösterim anında yapılır, böylece birim değişince yeniden istek atmak gerekmez.
 *
 * @property symbol Ekranda yazan kısa ad ("°C").
 * @property spokenName Ekran okuyucunun söyleyeceği ad.
 */
enum class TemperatureUnit(val symbol: String, val spokenName: String) {
    CELSIUS("°C", "Santigrat"),
    FAHRENHEIT("°F", "Fahrenheit");

    /** Celsius değeri bu birime çevirip en yakın tam sayıya yuvarlar. F = C × 9/5 + 32. */
    fun convert(celsius: Double): Int = when (this) {
        CELSIUS -> celsius.roundToInt()
        FAHRENHEIT -> (celsius * 9 / 5 + 32).roundToInt()
    }

    /** "18°" biçiminde, birim simgesi olmadan: kartlarda yer kazandırır. */
    fun format(celsius: Double): String = "${convert(celsius)}°"
}
