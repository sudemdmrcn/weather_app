package com.kampplus.hava.ui.cities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.R
import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.component.HavaCard
import com.kampplus.hava.ui.component.WeatherIcon
import com.kampplus.hava.ui.component.iconColor
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme
import com.kampplus.hava.ui.today.timeDifferenceLabel

/**
 * Şehirler listesindeki kayıtlı şehir kartı. İlk şehir ("Konumum") accent zeminli ve beyaz yazılı,
 * diğerleri yüzey zeminli ve çerçevelidir.
 *
 * Sol taraf: küçük etiket (varsa), şehir adı, "durum · Y 21° · D 13°". Sağ taraf: ikon ve büyük sıcaklık.
 * Etiket: ilk şehirde "Konumum"; diğerlerinde, ülkesi ilk şehirden farklıysa ülke adı ve telefonla
 * saat farkı ("Almanya · 2 saat geride").
 *
 * @param homeCountryCode İlk şehrin ülke kodu; etikette ülkenin ne zaman yazılacağını belirler.
 * @param deviceOffsetSeconds Telefonun UTC farkı; saat farkı etiketi için.
 */
@Composable
fun SavedCityCard(
    data: CityCardData,
    unit: TemperatureUnit,
    homeCountryCode: String?,
    deviceOffsetSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    val onAccent = data.isHome
    val primaryText = if (onAccent) colors.onAccent else colors.ink
    val secondaryText = if (onAccent) colors.onAccentSecondary else colors.muted
    val condition = data.summary?.let { WeatherCondition.fromWeatherCode(it.weatherCode) }
    val tag = cityTag(data, homeCountryCode, deviceOffsetSeconds, stringResource(R.string.my_location))

    HavaCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = if (onAccent) colors.accent else colors.surface,
        borderColor = if (onAccent) colors.accent else colors.border,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                tag?.let {
                    Text(
                        text = it,
                        style = WeatherText.caption12SemiBold,
                        color = if (onAccent) colors.onAccentSecondary else colors.accentOnSurface
                    )
                }
                Text(
                    text = data.city.name,
                    style = WeatherText.cityName,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.summary?.let { summaryLine(condition!!, it, unit) } ?: stringResource(R.string.cities_no_data),
                    style = WeatherText.caption13,
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (condition != null) {
                    WeatherIcon(condition = condition, size = 30.dp, tint = if (onAccent) colors.onAccent else condition.iconColor())
                    Text(text = unit.format(data.summary.temperatureC), style = WeatherText.cityTemp, color = primaryText)
                } else {
                    Text(text = stringResource(R.string.cities_no_data), style = WeatherText.cityTemp, color = secondaryText)
                }
            }
        }
    }
}

/** "Parçalı bulutlu · Y 21° · D 13°" */
private fun summaryLine(condition: WeatherCondition, summary: CitySummary, unit: TemperatureUnit) =
    "${condition.label} · Y ${unit.format(summary.maxTemperatureC)} · D ${unit.format(summary.minTemperatureC)}"

/** Kartın küçük etiketi; gösterilecek bir şey yoksa `null`. */
internal fun cityTag(data: CityCardData, homeCountryCode: String?, deviceOffsetSeconds: Int, homeLabel: String): String? {
    if (data.isHome) return homeLabel
    val country = data.city.country.takeIf { data.city.countryCode != homeCountryCode }
    val difference = data.summary?.let { timeDifferenceLabel(it.utcOffsetSeconds, deviceOffsetSeconds) }
    return listOfNotNull(country, difference).joinToString(" · ").ifEmpty { null }
}

@Preview(showBackground = true)
@Composable
private fun SavedCityCardPreview() {
    HavaTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val berlin = City(2950159L, "Berlin", 52.52, 13.41, "DE", "Almanya", "Berlin")
            SavedCityCard(
                data = CityCardData(DEFAULT_CITY, CitySummary(18.0, 2, 13.0, 21.0, 10800), isHome = true),
                unit = TemperatureUnit.CELSIUS,
                homeCountryCode = "TR",
                deviceOffsetSeconds = 10800,
                onClick = {}
            )
            SavedCityCard(
                data = CityCardData(berlin, CitySummary(9.0, 61, 6.0, 11.0, 3600), isHome = false),
                unit = TemperatureUnit.CELSIUS,
                homeCountryCode = "TR",
                deviceOffsetSeconds = 10800,
                onClick = {}
            )
        }
    }
}
