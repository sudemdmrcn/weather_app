package com.kampplus.hava.ui.cities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySummary
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.CitiesSkeleton
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme
import java.time.Instant
import java.time.ZoneId

/**
 * Şehirler ekranının bağlayıcı katmanı. Karta dokununca şehir seçilir ve [onCityClick] ile
 * Bugün sekmesine geçilir; "+" düğmesi [onAddClick] ile şehir ekleme akışını açar.
 */
@Composable
fun CitiesRoute(
    onAddClick: () -> Unit,
    onCityClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitiesViewModel = viewModel(factory = CitiesViewModel.Factory)
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unit by viewModel.unit.collectAsStateWithLifecycle()

    CitiesScreen(
        query = query,
        uiState = uiState,
        unit = unit,
        onQueryChange = viewModel::onQueryChange,
        onAddClick = onAddClick,
        onCityClick = { city ->
            viewModel.onCityClick(city)
            onCityClick()
        },
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

/**
 * Şehirler ekranı: başlık ve "+" düğmesi, arama kutusu, kayıtlı şehir kartları.
 * Dört durum: iskelet, liste, "Şehir bulunamadı" ve hata (tekrar dene).
 */
@Composable
fun CitiesScreen(
    query: String,
    uiState: LoadState<List<CityCardData>>,
    unit: TemperatureUnit,
    onQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCityClick: (City) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    // Telefonun UTC farkı; kartlardaki "2 saat geride" etiketi için.
    val deviceOffsetSeconds = remember { ZoneId.systemDefault().rules.getOffset(Instant.now()).totalSeconds }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.cities_title), style = WeatherText.pageTitle, color = colors.ink)
            Surface(onClick = onAddClick, modifier = Modifier.size(50.dp), shape = CircleShape, color = colors.accent) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = stringResource(R.string.action_add_city),
                        modifier = Modifier.size(24.dp),
                        tint = colors.onAccent
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = stringResource(R.string.cities_search_label), style = WeatherText.bodyMedium, color = colors.ink)
        Spacer(modifier = Modifier.height(8.dp))
        CitySearchField(query = query, onQueryChange = onQueryChange)
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                LoadState.Loading -> CitiesSkeleton()
                LoadState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    EmptyView(title = stringResource(R.string.cities_empty_title), message = stringResource(R.string.cities_empty_message))
                }
                is LoadState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    ErrorView(message = uiState.message, onRetry = onRetry)
                }
                is LoadState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val homeCountry = uiState.data.firstOrNull { it.isHome }?.city?.countryCode
                    items(items = uiState.data, key = { it.city.id }) { card ->
                        SavedCityCard(
                            data = card,
                            unit = unit,
                            homeCountryCode = homeCountry,
                            deviceOffsetSeconds = deviceOffsetSeconds,
                            onClick = { onCityClick(card.city) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Arama kutusu: 50dp yüksekliğinde, 16dp köşeli, yüzey zeminli. Yalnızca kayıtlı şehirleri süzer.
 * Görünür etiket üstte ayrı bir metin olduğundan, ekran okuyucuya da aynı ad verilir.
 */
@Composable
private fun CitySearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val label = stringResource(R.string.cities_search_label)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().height(50.dp).semantics { contentDescription = label },
        singleLine = true,
        textStyle = WeatherText.body.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.accentOnSurface),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(colors.surface)
                    .border(BorderStroke(1.dp, colors.border), shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colors.muted
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(text = stringResource(R.string.cities_search_placeholder), style = WeatherText.body, color = colors.muted)
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun CitiesScreenPreview() {
    HavaTheme {
        val berlin = City(2950159L, "Berlin", 52.52, 13.41, "DE", "Almanya", "Berlin")
        CitiesScreen(
            query = "",
            uiState = LoadState.Success(
                listOf(
                    CityCardData(DEFAULT_CITY, CitySummary(18.0, 2, 13.0, 21.0, 10800), isHome = true),
                    CityCardData(berlin, CitySummary(9.0, 61, 6.0, 11.0, 3600), isHome = false)
                )
            ),
            unit = TemperatureUnit.CELSIUS,
            onQueryChange = {},
            onAddClick = {},
            onCityClick = {},
            onRetry = {}
        )
    }
}
