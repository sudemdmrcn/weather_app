package com.kampplus.hava.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.R
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme

/** Alt gezinmedeki dört hedef. [route] navigasyon adresidir. */
enum class HavaTab(val route: String, @param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    TODAY("today", R.string.tab_today, R.drawable.ic_sun),
    CITIES("cities", R.string.tab_cities, R.drawable.ic_buildings),
    MAP("map", R.string.tab_map, R.drawable.ic_map),
    SETTINGS("settings", R.string.tab_settings, R.drawable.ic_sliders_horizontal)
}

/**
 * Alt gezinme çubuğu: yüzey zemin, üstte 1dp çerçeve, eşit genişlikte dört öğe (22dp ikon + 12sp etiket).
 * Aktif öğe accent renginde ve SemiBold, diğerleri soluk. Gölge yok.
 *
 * Sistem gezinme çubuğunun altına taşmaması için `navigationBarsPadding` kullanır.
 */
@Composable
fun HavaBottomBar(selected: HavaTab?, onSelect: (HavaTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        HorizontalDivider(color = colors.border)
        Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().selectableGroup()) {
            HavaTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val tint = if (isSelected) colors.accentOnSurface else colors.muted
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) })
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(painter = painterResource(tab.icon), contentDescription = null, modifier = Modifier.size(22.dp), tint = tint)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(tab.label),
                        style = if (isSelected) WeatherText.caption12SemiBold else WeatherText.caption12Medium,
                        color = tint
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HavaBottomBarPreview() {
    HavaTheme {
        HavaBottomBar(selected = HavaTab.TODAY, onSelect = {})
    }
}
