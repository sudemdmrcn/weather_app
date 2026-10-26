package com.kampplus.hava.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kampplus.hava.R
import com.kampplus.hava.ui.theme.WeatherText
import com.kampplus.hava.ui.theme.WeatherTheme

/**
 * Henüz tasarımı olmayan sekmeler (Harita, Ayarlar) için "Yakında" ekranı. Alt gezinmenin dört
 * hedefi de çalışsın ama boş/bozuk bir ekrana düşülmesin diye yer tutucu görevi görür.
 */
@Composable
fun ComingSoonScreen(@StringRes title: Int, @DrawableRes icon: Int, modifier: Modifier = Modifier) {
    val colors = WeatherTheme.colors
    Box(
        modifier = modifier.fillMaxSize().background(colors.bg).statusBarsPadding().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(48.dp), tint = colors.muted)
            Text(text = stringResource(title), style = WeatherText.pageTitle, color = colors.ink)
            Text(
                text = stringResource(R.string.coming_soon_title) + " · " + stringResource(R.string.coming_soon_message),
                style = WeatherText.body,
                color = colors.muted,
                textAlign = TextAlign.Center
            )
        }
    }
}
