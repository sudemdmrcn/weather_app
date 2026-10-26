package com.kampplus.hava.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Hava durumu ekranlarının renk jetonları.
 *
 * Mevcut Material paletinden ([Color.kt], [Theme.kt]) **ayrıdır**: spec'teki renkler burada
 * isimlendirilir, bileşenler hex yazmaz, `WeatherTheme.colors.xxx` okur. Açık/koyu seçimi
 * [HavaTheme] içinde yapılır; uygulamanın mevcut koyu tema geçişine bağlıdır.
 *
 * @property accent Hero kart ve seçili durumların zemin rengi. Zemin olarak her iki temada aynıdır.
 * @property accentOnSurface Accent'in **yazı/ikon** olarak koyu yüzey üzerinde kullanıldığı yerler
 *   için ton. Koyu temada `#2B5FD9`, `#17243A` üzerinde yalnızca ~2,8:1 kontrast verir
 *   (hedef 4,5:1); bu yüzden orada daha açık bir ton kullanılır. Açık temada accent ile aynıdır.
 */
@Immutable
class WeatherColors(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val accentOnSurface: Color,
    val iconSun: Color,
    val iconRain: Color,
    val iconCloud: Color,
    val illustrationSun: Color,
    val illustrationCloud: Color,
    val illustrationRain: Color
) {
    /** Accent zeminin üstündeki ikincil yazı: beyaz, %88 opaklık. */
    val onAccentSecondary: Color get() = onAccent.copy(alpha = 0.88f)
}

private val Accent = Color(0xFF2B5FD9)
private val AccentLightTint = Color(0xFF8DAEF5)

internal val LightWeatherColors = WeatherColors(
    bg = Color(0xFFF2F4F7),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF0F1B2D),
    muted = Color(0xFF5B6779),
    border = Color(0xFFE3E7ED),
    accent = Accent,
    onAccent = Color(0xFFFFFFFF),
    accentOnSurface = Accent,
    iconSun = Color(0xFFD9701A),
    iconRain = Accent,
    iconCloud = Color(0xFF5B6779),
    illustrationSun = Color(0xFFFFC857),
    illustrationCloud = Color(0xFFFFFFFF),
    illustrationRain = Color(0xFFCFE0FF)
)

internal val DarkWeatherColors = WeatherColors(
    bg = Color(0xFF0F1B2D),
    surface = Color(0xFF17243A),
    ink = Color(0xFFF2F4F7),
    muted = Color(0xFFA3AEBE),
    border = Color(0xFF26344D),
    accent = Accent,
    onAccent = Color(0xFFFFFFFF),
    accentOnSurface = AccentLightTint,
    iconSun = Color(0xFFD9701A),
    iconRain = AccentLightTint,
    iconCloud = Color(0xFFA3AEBE),
    illustrationSun = Color(0xFFFFC857),
    illustrationCloud = Color(0xFFFFFFFF),
    illustrationRain = Color(0xFFCFE0FF)
)

/** Tema içinde aktif olan weather renkleri. [HavaTheme] doldurur. */
internal val LocalWeatherColors = staticCompositionLocalOf { LightWeatherColors }

/** Bileşenlerin weather jetonlarını okuduğu tek kapı: `WeatherTheme.colors.accent`. */
object WeatherTheme {
    val colors: WeatherColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWeatherColors.current
}
