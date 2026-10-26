package com.kampplus.hava.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.data.SampleWeatherRepository
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.TodaySkeleton
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherTheme
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking

/**
 * Bugün ekranının bağlayıcı katmanı: ViewModel'i edinir, state'i toplar ve [TodayScreen]'e aktarır.
 * (Bkz. [com.kampplus.hava.ui.city.CityRoute], aynı desen.)
 */
@Composable
fun TodayRoute(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()
    val selectedHour by viewModel.selectedHour.collectAsStateWithLifecycle()

    TodayScreen(
        uiState = uiState,
        unit = unit,
        selectedHour = selectedHour,
        onUnitChange = viewModel::onUnitChange,
        onHourSelect = viewModel::onHourSelected,
        onSearchClick = onSearchClick,
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Bugün ekranı: `when` ile dört durumun her birine bir görünüm.
 * State tutmaz, aldığını çizer, olayları yukarı bildirir.
 */
@Composable
fun TodayScreen(
    uiState: LoadState<TodayContent>,
    unit: TemperatureUnit,
    selectedHour: Int,
    onUnitChange: (TemperatureUnit) -> Unit,
    onHourSelect: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(WeatherTheme.colors.bg)) {
        when (uiState) {
            LoadState.Loading -> TodaySkeleton()
            LoadState.Empty -> CenteredMessage { EmptyView(title = stringResource(R.string.forecast_empty)) }
            is LoadState.Error -> CenteredMessage { ErrorView(message = uiState.message, onRetry = onRetry) }
            is LoadState.Success -> TodayContentView(
                content = uiState.data,
                unit = unit,
                selectedHour = selectedHour,
                onUnitChange = onUnitChange,
                onHourSelect = onHourSelect,
                onSearchClick = onSearchClick
            )
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun TodayContentView(
    content: TodayContent,
    unit: TemperatureUnit,
    selectedHour: Int,
    onUnitChange: (TemperatureUnit) -> Unit,
    onHourSelect: (Int) -> Unit,
    onSearchClick: () -> Unit
) {
    val forecast = content.forecast
    val slots = remember(forecast) { buildHourSlots(forecast) }
    val slot = slots.getOrElse(selectedHour) { slots.first() }
    val today = forecast.daily.firstOrNull()
    val details = detailTiles(current = forecast.current, today = today)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WeatherHeader(
            city = content.city,
            isHome = content.isHome,
            date = forecast.current.observedAt.toLocalDate(),
            unit = unit,
            onUnitChange = onUnitChange,
            onSearchClick = onSearchClick,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        HeroCard(slot = slot, today = today, unit = unit, modifier = Modifier.padding(horizontal = 20.dp))
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(
                icon = R.drawable.ic_wind,
                label = stringResource(R.string.metric_wind),
                value = slot.windSpeedKmh?.rounded() ?: DASH,
                unit = slot.windSpeedKmh?.let { stringResource(R.string.wind_unit) },
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = R.drawable.ic_droplets,
                label = stringResource(R.string.metric_humidity),
                value = slot.humidityPercent?.let { "%$it" } ?: DASH,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = R.drawable.ic_umbrella,
                label = stringResource(R.string.metric_rain),
                value = slot.precipitationProbability?.let { "%$it" } ?: DASH,
                modifier = Modifier.weight(1f)
            )
        }
        HourlyStrip(slots = slots, selectedIndex = selectedHour, unit = unit, onSelect = onHourSelect)
        DailyForecastCard(days = forecast.daily, unit = unit)
        // Servis bu alanları göndermediyse bölüm hiç çizilmez; boş kart göstermeyiz.
        if (details.isNotEmpty()) DetailsCard(tiles = details)
    }
}

private const val DASH = "—"

/**
 * Bugünün ek ölçümlerini kutucuklara çevirir; değeri olmayan ölçüm listeye girmez.
 * Anlık veriden: rüzgâr yönü, basınç, görüş. Bugünün günlük verisinden: UV, gün doğumu/batımı.
 */
@Composable
private fun detailTiles(current: CurrentWeather, today: DailyForecast?): List<DetailTile> {
    val timeFormat = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm") }
    return listOfNotNull(
        today?.uvIndexMax?.let { DetailTile(stringResource(R.string.detail_uv), it.rounded(), uvLevelLabel(it)) },
        current.windDirectionDegrees?.let {
            DetailTile(stringResource(R.string.detail_wind_direction), windDirectionLabel(it), "$it°")
        },
        today?.sunrise?.let { DetailTile(stringResource(R.string.detail_sunrise), it.format(timeFormat)) },
        today?.sunset?.let { DetailTile(stringResource(R.string.detail_sunset), it.format(timeFormat)) },
        current.pressureHpa?.let { DetailTile(stringResource(R.string.detail_pressure), "${it.rounded()} hPa") },
        current.visibilityMeters?.let { DetailTile(stringResource(R.string.detail_visibility), visibilityLabel(it)) }
    )
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun TodayScreenPreview() {
    HavaTheme {
        val forecast = runBlocking { SampleWeatherRepository { LocalDateTime.of(2026, 10, 25, 13, 30) }.getForecast(0.0, 0.0) }
        TodayScreen(
            uiState = LoadState.Success(TodayContent(DEFAULT_CITY, isHome = true, forecast = forecast)),
            unit = TemperatureUnit.CELSIUS,
            selectedHour = 0,
            onUnitChange = {},
            onHourSelect = {},
            onSearchClick = {},
            onRetry = {}
        )
    }
}
