package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Uygulamanın renk paleti. MaterialTheme sayesinde renkler tek yerden yönetilir.
 *
 * Renkler burada **isimlendirilir**, [Theme] dosyasında açık/koyu temaya dağıtılır.
 * Bileşenler hiçbir zaman buradaki sabitleri doğrudan kullanmaz; `MaterialTheme.colorScheme`
 * üzerinden okur. Böylece tek satır değişince bütün uygulama yeni kıyafetini giyer.
 *
 * İsimlerdeki sayı tonu belirtir: 10 en koyu, 99 en açık.
 */

// Ana vurgu: gökyüzü mavisi.
internal val Blue10 = Color(0xFF001B3E)
internal val Blue20 = Color(0xFF002F65)
internal val Blue30 = Color(0xFF0B4A8F)
internal val Blue40 = Color(0xFF1565C0)
internal val Blue80 = Color(0xFFA8C8FF)
internal val Blue90 = Color(0xFFD9E6FB)

// İkincil vurgu: güneş sarısı/turuncusu.
internal val Amber40 = Color(0xFF8B5000)
internal val Amber80 = Color(0xFFFFB86E)
internal val Amber90 = Color(0xFFFFDCBE)

// Açık tema yüzeyleri: hafif mavi-gri zemin, beyaz kartlar.
internal val Slate10 = Color(0xFF111827)
internal val Slate50 = Color(0xFF5B6673)
internal val Slate90 = Color(0xFFDDE3EA)
internal val Slate95 = Color(0xFFEBEFF4)
internal val Slate98 = Color(0xFFF4F7FB)

// Koyu tema yüzeyleri: derin lacivert zemin, bir ton açık kartlar.
internal val Night05 = Color(0xFF0E141B)
internal val Night10 = Color(0xFF18212B)
internal val Night20 = Color(0xFF2A3541)
internal val Night80 = Color(0xFF9AA7B5)
internal val Night95 = Color(0xFFE6EBF1)

internal val Red40 = Color(0xFFBA1A1A)
internal val Red80 = Color(0xFFFFB4AB)
