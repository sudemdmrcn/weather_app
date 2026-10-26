package com.kampplus.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Yazı tipi ölçeği. Tema üç dosyadan oluşur: Color, Theme, Type.
 *
 * Material 3'ün hazır ölçeğinden başlayıp iki şeyi değiştiriyoruz:
 * - Başlıklar biraz daha kalın: ekranın hiyerarşisi bir bakışta okunur.
 * - Büyük sıcaklık rakamı ince (Light) ve iri: hava durumu uygulamalarının imzası.
 *
 * Bileşenler boyutu elle yazmaz; `MaterialTheme.typography.titleMedium` gibi
 * **rol** isimleriyle okur. Böylece tipografi tek yerden değişir.
 */
private val base = Typography()

internal val HavaTypography = base.copy(
    displayLarge = base.displayLarge.copy(fontSize = 72.sp, fontWeight = FontWeight.Light),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.Bold)
)
