package com.kampplus.hava.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme

/**
 * °C / °F segmentli geçişi: boşluk rengi çerçeve tonunda bir hap, içinde iki 44dp'lik yuvarlak düğme.
 * Seçili düğme yüzey renginde ve koyu yazılı, diğeri saydam ve soluk yazılı.
 *
 * `selectable` + `Role.RadioButton`: TalkBack "Santigrat, seçili" diye okur.
 */
@Composable
fun UnitToggle(unit: TemperatureUnit, onUnitChange: (TemperatureUnit) -> Unit, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.border)
            .padding(3.dp)
            .selectableGroup()
    ) {
        TemperatureUnit.entries.forEach { option ->
            val selected = option == unit
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.surface else Color.Transparent)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onUnitChange(option) })
                    .semantics { contentDescription = option.spokenName },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.symbol,
                    style = WeatherText.bodySemiBold,
                    color = if (selected) colors.ink else colors.muted
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnitTogglePreview() {
    HavaTheme {
        UnitToggle(unit = TemperatureUnit.CELSIUS, onUnitChange = {})
    }
}
