package com.kampplus.hava.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.kampplus.hava.R

/**
 * Space Grotesk (sayılar ve başlıklar) ve Inter (gövde ve arayüz).
 *
 * İki dosya da "variable" font: tek `.ttf` içinde tüm kalınlıklar var. Her kalınlık için
 * `FontVariation.weight` ile ayrı bir `Font` kaydı veriyoruz (minSdk 26 bunu destekler).
 * Lisans: SIL Open Font License 1.1.
 */
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight) =
    Font(resId, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val SpaceGrotesk = FontFamily(
    variable(R.font.space_grotesk, FontWeight.Medium),
    variable(R.font.space_grotesk, FontWeight.SemiBold)
)

val Inter = FontFamily(
    variable(R.font.inter, FontWeight.Normal),
    variable(R.font.inter, FontWeight.Medium),
    variable(R.font.inter, FontWeight.SemiBold)
)

/**
 * Spec'teki metin stilleri. Boyutlar sp, harf aralığı `em` (yazı boyutuna oranlı).
 * Bileşenler boyut yazmaz, bu isimlerden birini kullanır.
 */
object WeatherText {
    /** Sayfa başlığı ("İstanbul", "Şehirler"). */
    val pageTitle = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em
    )

    /** Hero kartındaki dev sıcaklık; satır yüksekliği bilerek dar. */
    val heroTemp = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 96.sp,
        lineHeight = 92.sp,
        letterSpacing = (-0.04).em
    )
    val heroCondition = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    )
    val sectionTitle = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
    val body = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val bodyMedium = body.copy(fontWeight = FontWeight.Medium)
    val bodySemiBold = body.copy(fontWeight = FontWeight.SemiBold)
    val caption13 = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
    val caption12 = caption13.copy(fontSize = 12.sp, lineHeight = 16.sp)
    val caption12Medium = caption12.copy(fontWeight = FontWeight.Medium)
    val caption12SemiBold = caption12.copy(fontWeight = FontWeight.SemiBold)

    /** Kart içi sayılar: 18sp. */
    val cardNumber = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em
    )
    val cityName = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.02).em
    )
    val cityTemp = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).em
    )
}
