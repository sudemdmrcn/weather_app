package com.kampplus.hava.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.WeatherTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Hero kartındaki düz illüstrasyonun dört türü. */
enum class IllustrationKind { SUN, PARTLY, CLOUDS, RAIN }

fun WeatherCondition.illustrationKind(): IllustrationKind = when (this) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR -> IllustrationKind.SUN
    WeatherCondition.PARTLY_CLOUDY -> IllustrationKind.PARTLY
    WeatherCondition.OVERCAST, WeatherCondition.FOG, WeatherCondition.UNKNOWN -> IllustrationKind.CLOUDS
    else -> IllustrationKind.RAIN
}

/**
 * Hava durumuna uygun 120dp düz illüstrasyon: güneş / güneş+bulut / iki bulut / bulut+yağmur.
 *
 * Gradyan, gölge ve çizgi yok; yalnızca düz renkli şekiller ([WeatherTheme] illüstrasyon
 * jetonları). Süs olduğu için TalkBack'ten gizlenir; durum zaten yazıyla söyleniyor.
 */
@Composable
fun WeatherIllustration(condition: WeatherCondition, modifier: Modifier = Modifier, size: Dp = 120.dp) {
    val colors = WeatherTheme.colors
    val sun = colors.illustrationSun
    val cloud = colors.illustrationCloud
    val rain = colors.illustrationRain
    val kind = condition.illustrationKind()
    Canvas(modifier = modifier.size(size).clearAndSetSemantics {}) {
        val s = this.size.minDimension
        when (kind) {
            IllustrationKind.SUN -> drawSun(Offset(s * 0.5f, s * 0.5f), s * 0.22f, sun)
            IllustrationKind.PARTLY -> {
                drawSun(Offset(s * 0.38f, s * 0.36f), s * 0.17f, sun)
                drawCloud(Offset(s * 0.16f, s * 0.42f), s * 0.7f, cloud)
            }
            IllustrationKind.CLOUDS -> {
                drawCloud(Offset(s * 0.04f, s * 0.2f), s * 0.58f, cloud.copy(alpha = 0.65f))
                drawCloud(Offset(s * 0.26f, s * 0.4f), s * 0.7f, cloud)
            }
            IllustrationKind.RAIN -> {
                drawCloud(Offset(s * 0.14f, s * 0.16f), s * 0.72f, cloud)
                val stroke = s * 0.05f
                listOf(0.34f, 0.5f, 0.66f).forEach { x ->
                    drawLine(
                        color = rain,
                        start = Offset(s * x, s * 0.7f),
                        end = Offset(s * (x - 0.05f), s * 0.88f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

/** Güneş: dolu daire + 8 kısa ışın. */
private fun DrawScope.drawSun(center: Offset, radius: Float, color: Color) {
    drawCircle(color = color, radius = radius, center = center)
    repeat(8) { i ->
        val angle = i * PI / 4
        val dx = cos(angle).toFloat()
        val dy = sin(angle).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x + dx * radius * 1.38f, center.y + dy * radius * 1.38f),
            end = Offset(center.x + dx * radius * 1.75f, center.y + dy * radius * 1.75f),
            strokeWidth = radius * 0.22f,
            cap = StrokeCap.Round
        )
    }
}

/** Bulut: yuvarlak köşeli gövde + üstte iki kabarık daire. [topLeft] gövdenin sol üst kutusudur. */
private fun DrawScope.drawCloud(topLeft: Offset, width: Float, color: Color) {
    val height = width * 0.56f
    drawRoundRect(
        color = color,
        topLeft = Offset(topLeft.x, topLeft.y + height * 0.38f),
        size = Size(width, height * 0.62f),
        cornerRadius = CornerRadius(height * 0.31f)
    )
    drawCircle(color, radius = height * 0.3f, center = Offset(topLeft.x + width * 0.32f, topLeft.y + height * 0.46f))
    drawCircle(color, radius = height * 0.4f, center = Offset(topLeft.x + width * 0.6f, topLeft.y + height * 0.4f))
}
