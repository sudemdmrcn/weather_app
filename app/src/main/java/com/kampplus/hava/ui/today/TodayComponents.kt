package com.kampplus.hava.ui.today

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.component.HavaCard
import com.kampplus.hava.ui.component.UnitToggle
import com.kampplus.hava.ui.component.WeatherIcon
import com.kampplus.hava.ui.component.WeatherIllustration
import com.kampplus.hava.ui.component.iconColor
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val CARD_SHAPE = RoundedCornerShape(24.dp)
private val METRIC_SHAPE = RoundedCornerShape(20.dp)

/**
 * Sayfanın başı: sol tarafta konum satırı, şehir adı ve tarih; sağ tarafta °C/°F geçişi ile
 * arama düğmesi.
 *
 * Konum satırında "Konumum" etiketi yalnızca ilk kayıtlı şehirde görünür. Şehir uzunsa
 * konum metni kısalır, etiket her zaman okunur kalır.
 */
@Composable
internal fun WeatherHeader(
    city: City,
    isHome: Boolean,
    date: LocalDate,
    unit: TemperatureUnit,
    onUnitChange: (TemperatureUnit) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_map_pin),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colors.muted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = city.subtitle.ifEmpty { city.name },
                    style = WeatherText.caption13,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isHome) {
                    Text(
                        text = " · " + stringResource(R.string.my_location),
                        style = WeatherText.caption13,
                        color = colors.muted,
                        maxLines = 1
                    )
                }
            }
            Text(text = city.name, style = WeatherText.pageTitle, color = colors.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = dateLabel(date), style = WeatherText.caption13, color = colors.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            UnitToggle(unit = unit, onUnitChange = onUnitChange)
            Surface(
                onClick = onSearchClick,
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = stringResource(R.string.action_search_city),
                        modifier = Modifier.size(22.dp),
                        tint = colors.ink
                    )
                }
            }
        }
    }
}

/**
 * Accent zeminli ana kart (düz renk, gölge yok): sol üstte etiket ve dev sıcaklık, sağ üstte
 * hava durumu illüstrasyonu, altta durum + günün en yüksek/en düşüğü ve hissedilen sıcaklık.
 *
 * Saatlik şeritten bir saat seçilince [slot] değişir ve kartın tamamı o saate göre güncellenir.
 */
@Composable
internal fun HeroCard(slot: HourSlot, today: DailyForecast?, unit: TemperatureUnit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    val condition = WeatherCondition.fromWeatherCode(slot.weatherCode)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colors.accent)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = if (slot.isNow) {
                        stringResource(R.string.hero_now)
                    } else {
                        stringResource(R.string.hero_at_hour, slot.time.format(HOUR_FORMAT))
                    },
                    style = WeatherText.caption13,
                    color = colors.onAccentSecondary
                )
                Text(text = unit.format(slot.temperatureC), style = WeatherText.heroTemp, color = colors.onAccent)
            }
            WeatherIllustration(condition = condition)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(text = condition.label, style = WeatherText.heroCondition, color = colors.onAccent)
                today?.let {
                    Text(
                        text = stringResource(R.string.detail_high_low, unit.format(it.maxTemperatureC), unit.format(it.minTemperatureC)),
                        style = WeatherText.body,
                        color = colors.onAccentSecondary
                    )
                }
            }
            slot.apparentTemperatureC?.let {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.detail_feels_like), style = WeatherText.caption13, color = colors.onAccentSecondary)
                    Text(text = unit.format(it), style = WeatherText.cardNumber, color = colors.onAccent)
                }
            }
        }
    }
}

/** Üç eşit sütunlu ölçüm kartlarından biri: accent ikon, etiket ve değer (+ küçük birim). */
@Composable
internal fun MetricCard(@DrawableRes icon: Int, label: String, value: String, modifier: Modifier = Modifier, unit: String? = null) {
    val colors = WeatherTheme.colors
    HavaCard(modifier = modifier, containerColor = colors.surface, borderColor = colors.border, shape = METRIC_SHAPE) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp), tint = colors.accentOnSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, style = WeatherText.caption12, color = colors.muted)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = WeatherText.cardNumber, color = colors.ink)
                unit?.let {
                    Text(text = " $it", style = WeatherText.caption12, color = colors.muted, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}

/** "Saatlik" başlığı ve yatay kaydırılan saat hapları. */
@Composable
internal fun HourlyStrip(
    slots: List<HourSlot>,
    selectedIndex: Int,
    unit: TemperatureUnit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = stringResource(R.string.hourly_title), style = WeatherText.sectionTitle, color = colors.ink)
            Text(text = stringResource(R.string.hourly_hint), style = WeatherText.caption13, color = colors.muted)
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(items = slots, key = { _, slot -> slot.time.toString() }) { index, slot ->
                HourPill(slot = slot, selected = index == selectedIndex, unit = unit, onClick = { onSelect(index) })
            }
        }
    }
}

/**
 * Saat hapı (64×116dp): saat, ikon, sıcaklık. Seçili hap accent zeminli ve beyaz yazılı.
 * TalkBack tek bir cümle okur ("Şimdi, 18 derece, Parçalı bulutlu, seçili").
 */
@Composable
internal fun HourPill(slot: HourSlot, selected: Boolean, unit: TemperatureUnit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    val condition = WeatherCondition.fromWeatherCode(slot.weatherCode)
    val label = if (slot.isNow) stringResource(R.string.hourly_now) else slot.time.format(HOUR_FORMAT)
    val shape = RoundedCornerShape(22.dp)
    val textColor = if (selected) colors.onAccent else colors.ink
    val spoken = "$label, ${unit.convert(slot.temperatureC)} derece, ${condition.label}"
    Column(
        modifier = modifier
            .size(width = 64.dp, height = 116.dp)
            .clip(shape)
            .background(if (selected) colors.accent else colors.surface)
            .border(1.dp, if (selected) colors.accent else colors.border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = spoken }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = WeatherText.caption12Medium, color = if (selected) colors.onAccentSecondary else colors.muted)
        WeatherIcon(condition = condition, tint = if (selected) colors.onAccent else condition.iconColor())
        Text(text = unit.format(slot.temperatureC), style = WeatherText.cardNumber, color = textColor)
    }
}

/** "7 günlük tahmin" başlığı ve satırların durduğu kart. Seçili gün yalnızca vurgulanır. */
@Composable
internal fun DailyForecastCard(days: List<DailyForecast>, unit: TemperatureUnit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    var selectedDay by rememberSaveable { mutableIntStateOf(0) }
    val weekMin = days.minOfOrNull { it.minTemperatureC } ?: 0.0
    val weekMax = days.maxOfOrNull { it.maxTemperatureC } ?: 0.0
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.daily_title),
            style = WeatherText.sectionTitle,
            color = colors.ink,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        HavaCard(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            containerColor = colors.surface,
            borderColor = colors.border,
            shape = CARD_SHAPE
        ) {
            days.forEachIndexed { index, day ->
                if (index > 0) HorizontalDivider(color = colors.border)
                DayRow(
                    day = day,
                    isToday = index == 0,
                    selected = index == selectedDay,
                    unit = unit,
                    weekMin = weekMin,
                    weekMax = weekMax,
                    onClick = { selectedDay = index }
                )
            }
        }
    }
}

/** Gün satırı: gün adı · ikon · yağış % · en düşük · aralık çubuğu · en yüksek. Yüksekliği en az 48dp. */
@Composable
internal fun DayRow(
    day: DailyForecast,
    isToday: Boolean,
    selected: Boolean,
    unit: TemperatureUnit,
    weekMin: Double,
    weekMax: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WeatherTheme.colors
    val condition = WeatherCondition.fromWeatherCode(day.weatherCode)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (selected) colors.bg else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else dayShortLabel(day.date),
            style = WeatherText.bodyMedium,
            color = colors.ink,
            modifier = Modifier.width(52.dp)
        )
        WeatherIcon(condition = condition)
        Box(modifier = Modifier.width(44.dp), contentAlignment = Alignment.CenterEnd) {
            // Yağış olasılığı sıfırsa gösterilmez; alan yine de ayrılır, sütunlar hizalı kalır.
            rainPercent(day.precipitationProbability)?.let {
                Text(text = it, style = WeatherText.caption12SemiBold, color = colors.accentOnSurface)
            }
        }
        Text(
            text = unit.format(day.minTemperatureC),
            style = WeatherText.body,
            color = colors.muted,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
        TemperatureRange(
            min = day.minTemperatureC,
            max = day.maxTemperatureC,
            weekMin = weekMin,
            weekMax = weekMax,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        Text(
            text = unit.format(day.maxTemperatureC),
            style = WeatherText.bodySemiBold,
            color = colors.ink,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
    }
}

/**
 * Günün sıcaklık aralığını haftaya göre gösteren 6dp'lik çubuk: çerçeve renginde şerit
 * haftanın tamamıdır, accent dolgu o günün en düşük–en yüksek aralığıdır.
 * Dolgunun yeri `weight` ile oranlanır: sol boşluk + dolgu + sağ boşluk.
 */
@Composable
private fun TemperatureRange(min: Double, max: Double, weekMin: Double, weekMax: Double, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    val span = (weekMax - weekMin).takeIf { it > 0.0 } ?: 1.0
    val start = ((min - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val end = ((max - weekMin) / span).toFloat().coerceIn(0f, 1f)
    // weight sıfırdan büyük olmak zorunda; çok kısa aralıklar da görünür kalsın.
    val leading = start.coerceAtLeast(MIN_WEIGHT)
    val fill = (end - start).coerceAtLeast(MIN_WEIGHT)
    val trailing = (1f - end).coerceAtLeast(MIN_WEIGHT)
    Row(modifier = modifier.height(6.dp).clip(CircleShape).background(colors.border)) {
        Spacer(modifier = Modifier.weight(leading))
        Box(modifier = Modifier.weight(fill).fillMaxSize().clip(CircleShape).background(colors.accent))
        Spacer(modifier = Modifier.weight(trailing))
    }
}

/** "Detaylar" kartındaki tek ölçüm: başlık, değer ve isteğe bağlı küçük açıklama. */
internal data class DetailTile(val label: String, val value: String, val caption: String? = null)

/** İki sütunlu ölçüm kartı; kutucuklar ikişer ikişer satırlara dizilir. */
@Composable
internal fun DetailsCard(tiles: List<DetailTile>, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.details_title),
            style = WeatherText.sectionTitle,
            color = colors.ink,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        HavaCard(
            modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
            containerColor = colors.surface,
            borderColor = colors.border,
            shape = CARD_SHAPE
        ) {
            tiles.chunked(2).forEachIndexed { rowIndex, pair ->
                if (rowIndex > 0) HorizontalDivider(color = colors.border)
                Row(modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { tile -> DetailTileView(tile = tile, modifier = Modifier.weight(1f)) }
                    // Tek kalan son kutucuk yarım genişlikte kalsın diye boş yer ayrılır.
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailTileView(tile: DetailTile, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = tile.label, style = WeatherText.caption12, color = colors.muted)
        Text(text = tile.value, style = WeatherText.cardNumber, color = colors.ink)
        // Açıklama yoksa da aynı yükseklik kalsın diye boşluk basılır.
        Text(text = tile.caption ?: " ", style = WeatherText.caption12, color = colors.muted)
    }
}

/** Yağış olasılığı yalnızca sıfırdan büyükse anlamlıdır. */
internal fun rainPercent(value: Int?): String? = value?.takeIf { it > 0 }?.let { "%$it" }

private const val MIN_WEIGHT = 0.02f

/** 18.4 → "18". Metrik değerler için yuvarlama. */
internal fun Double.rounded(): String = roundToInt().toString()
