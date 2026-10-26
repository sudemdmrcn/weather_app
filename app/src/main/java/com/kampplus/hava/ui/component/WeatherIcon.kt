package com.kampplus.hava.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.hava.R
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.WeatherTheme

/** Hava durumunun çizgi ikonu (Lucide). Emoji kullanılmaz. */
@DrawableRes
fun WeatherCondition.iconRes(): Int = when (this) {
    WeatherCondition.CLEAR -> R.drawable.ic_sun
    WeatherCondition.MAINLY_CLEAR, WeatherCondition.PARTLY_CLOUDY -> R.drawable.ic_cloud_sun
    WeatherCondition.OVERCAST, WeatherCondition.UNKNOWN -> R.drawable.ic_cloud
    WeatherCondition.FOG -> R.drawable.ic_cloud_fog
    WeatherCondition.DRIZZLE -> R.drawable.ic_cloud_drizzle
    WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> R.drawable.ic_cloud_rain
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> R.drawable.ic_cloud_snow
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_cloud_lightning
}

/** İkon rengi: güneş turuncu, yağış mavi, bulut/parçalı gri (spec). */
@Composable
fun WeatherCondition.iconColor(): Color {
    val colors = WeatherTheme.colors
    return when (this) {
        WeatherCondition.CLEAR -> colors.iconSun
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS,
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS, WeatherCondition.THUNDERSTORM -> colors.iconRain
        else -> colors.iconCloud
    }
}

/**
 * Duruma uygun renkli ikon. Yanında her zaman metin etiketi olduğu için varsayılan olarak
 * süs sayılır (`contentDescription = null`) ve TalkBack atlar.
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = condition.iconColor(),
    contentDescription: String? = null
) {
    Icon(
        painter = painterResource(condition.iconRes()),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}
