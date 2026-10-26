package com.kampplus.hava.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Uygulamanın standart kartı: yuvarlak köşe, ince çerçeve, gölge yok.
 *
 * Tüm ekranlar `Card` yerine bunu kullanır; böylece kartların görünümü tek yerden
 * değişir. [onClick] verilirse kart dokunulabilir olur.
 *
 * @param containerColor Varsayılan kart zemini (`surface`); vurgulu kartlar için değiştirilir.
 * @param borderColor Çerçeve rengi. `Color.Transparent` verilirse çerçeve çizilmez gibi görünür.
 * @param shape Köşe yuvarlaklığı; hava durumu kartları 20-24dp kullanır.
 */
@Composable
fun HavaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}
