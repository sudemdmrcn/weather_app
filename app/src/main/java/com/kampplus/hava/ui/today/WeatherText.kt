package com.kampplus.hava.ui.today

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Ham ölçüm değerlerini kullanıcının okuyacağı Türkçe metne çeviren saf fonksiyonlar.
 *
 * Android'e bağımlı olmadıkları için JVM testlerinde doğrudan denenebilirler
 * (bkz. `WeatherTextTest`).
 */

private val COMPASS = listOf(
    "Kuzey",
    "Kuzeydoğu",
    "Doğu",
    "Güneydoğu",
    "Güney",
    "Güneybatı",
    "Batı",
    "Kuzeybatı"
)

/**
 * Derece cinsinden rüzgâr yönünü pusula adına çevirir.
 *
 * Daire 8 dilime bölünür, her dilim 45° genişliğindedir ve adın etrafında ortalanır:
 * 337.5°–22.5° kuzey, 22.5°–67.5° kuzeydoğu... `+ 22.5` kaydırması kuzeyin 0°'nin iki
 * yanına taşmasını (350° ve 10° ikisi de kuzey) tek formülle çözer.
 */
fun windDirectionLabel(degrees: Int): String {
    val normalized = ((degrees % 360) + 360) % 360
    return COMPASS[((normalized + 22.5) / 45).toInt() % 8]
}

/** UV indeksini Dünya Sağlık Örgütü'nün risk kademelerine çevirir. */
fun uvLevelLabel(index: Double): String = when {
    index < 3 -> "Düşük"
    index < 6 -> "Orta"
    index < 8 -> "Yüksek"
    index < 11 -> "Çok yüksek"
    else -> "Aşırı"
}

/** Görüş mesafesi: 10 km altında bir ondalık ("4,5 km"), üstünde tam sayı ("24 km"). */
fun visibilityLabel(meters: Double): String {
    val km = meters / 1000
    return if (km < 10) {
        String.format(Locale.forLanguageTag("tr"), "%.1f km", km)
    } else {
        "${km.roundToInt()} km"
    }
}

private val DAY_SHORT = mapOf(
    DayOfWeek.MONDAY to "Pzt",
    DayOfWeek.TUESDAY to "Sal",
    DayOfWeek.WEDNESDAY to "Çar",
    DayOfWeek.THURSDAY to "Per",
    DayOfWeek.FRIDAY to "Cum",
    DayOfWeek.SATURDAY to "Cmt",
    DayOfWeek.SUNDAY to "Paz"
)

/** Günlük listedeki kısa gün adı: Pzt, Sal, Çar, Per, Cum, Cmt, Paz. */
fun dayShortLabel(date: LocalDate): String = DAY_SHORT.getValue(date.dayOfWeek)

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("tr"))

/** Başlıktaki tarih: "Pazar, 25 Ekim". Şehrin yerel tarihi verilmelidir. */
fun dateLabel(date: LocalDate): String = DATE_FORMAT.format(date)

/**
 * İki saat dilimi arasındaki farkı yazıya çevirir: "2 saat geride" / "1 saat ileride".
 * Fark yoksa `null` döner (kartta etiket gösterilmez).
 *
 * @param cityOffsetSeconds Şehrin UTC'den farkı.
 * @param deviceOffsetSeconds Telefonun UTC'den farkı.
 */
fun timeDifferenceLabel(cityOffsetSeconds: Int, deviceOffsetSeconds: Int): String? {
    val diffSeconds = cityOffsetSeconds - deviceOffsetSeconds
    if (diffSeconds == 0) return null
    val hours = kotlin.math.abs(diffSeconds) / 3600.0
    // Tam saat farkı "2", yarım saatlik dilimler (Hindistan) "5,5" yazılır.
    val amount = if (hours % 1.0 == 0.0) hours.toInt().toString() else String.format(Locale.forLanguageTag("tr"), "%.1f", hours)
    return if (diffSeconds < 0) "$amount saat geride" else "$amount saat ileride"
}
