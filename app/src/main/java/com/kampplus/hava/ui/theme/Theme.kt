package com.kampplus.hava.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Açık tema paleti. Her Material rolü ([Color.kt]'deki) bir renge bağlanır:
 * `primary` ana vurgu, `surface` kart zemini, `onSurface` o zemindeki yazı rengidir.
 *
 * Zemin (`background`) hafif gri-mavi, kartlar (`surface`) beyazdır; kartlar zeminden
 * gölge yerine ince bir çerçeve (`outlineVariant`) ile ayrılır.
 */
private val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Amber40,
    secondaryContainer = Amber90,
    background = Slate98,
    onBackground = Slate10,
    surface = Color.White,
    onSurface = Slate10,
    surfaceVariant = Slate95,
    onSurfaceVariant = Slate50,
    outlineVariant = Slate90,
    error = Red40
)

/** Koyu tema paleti. Aynı roller, koyu zemine uygun tonlarla. */
private val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Amber80,
    background = Night05,
    onBackground = Night95,
    surface = Night10,
    onSurface = Night95,
    surfaceVariant = Night20,
    onSurfaceVariant = Night80,
    outlineVariant = Night20,
    error = Red80
)

/**
 * Uygulamanın teması. Bütün ekranlar bunun içine sarmalanır.
 *
 * @param darkTheme Varsayılan olarak telefonun sistem ayarını izler
 *   ([isSystemInDarkTheme]). @Preview'larda elle `true` verip koyu temayı deneyebilirsin.
 */
@Composable
fun HavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // Weather jetonları Material paletinden ayrıdır ama aynı açık/koyu geçişine bağlıdır.
    CompositionLocalProvider(
        LocalWeatherColors provides if (darkTheme) DarkWeatherColors else LightWeatherColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = HavaTypography,
            content = content
        )
    }
}
