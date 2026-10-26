package com.kampplus.hava.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.WeatherTheme

/**
 * Soldan sağa kayan ışıltı fırçası; renkler weather jetonlarından (çerçeve ve yüzey tonu) gelir.
 * Tüm iskelet parçaları aynı fırçayı paylaşırsa ışıltı ekranda tek parça gibi akar.
 */
@Composable
private fun rememberShimmerBrush(): Brush {
    val colors = WeatherTheme.colors
    val translate by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = SHIMMER_DISTANCE,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(colors.border, colors.surface, colors.border),
        start = Offset(translate - SHIMMER_WIDTH, 0f),
        end = Offset(translate, 0f)
    )
}

private const val SHIMMER_DISTANCE = 1_500f
private const val SHIMMER_WIDTH = 400f

@Composable
private fun SkeletonBlock(brush: Brush, modifier: Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    Box(modifier = modifier.background(brush, shape))
}

/**
 * Bugün ekranının iskeleti: başlık, hero kart, üç ölçüm kartı, saat hapları ve günlük kart.
 * Ölçüleri gerçek bileşenlerle aynıdır; veri gelince ekran zıplamaz.
 */
@Composable
fun TodaySkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp)
            .clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBlock(brush, Modifier.size(width = 130.dp, height = 14.dp))
                SkeletonBlock(brush, Modifier.size(width = 170.dp, height = 32.dp))
                SkeletonBlock(brush, Modifier.size(width = 110.dp, height = 14.dp))
            }
            SkeletonBlock(brush, Modifier.size(width = 94.dp, height = 50.dp), CircleShape)
        }
        SkeletonBlock(brush, Modifier.fillMaxWidth().height(250.dp), RoundedCornerShape(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                SkeletonBlock(brush, Modifier.weight(1f).height(92.dp), RoundedCornerShape(20.dp))
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), userScrollEnabled = false) {
            items(5) {
                SkeletonBlock(brush, Modifier.size(width = 64.dp, height = 116.dp), RoundedCornerShape(22.dp))
            }
        }
        SkeletonBlock(brush, Modifier.fillMaxWidth().height(200.dp), RoundedCornerShape(24.dp))
    }
}

/** Şehirler ekranının iskeleti: gerçek kartlarla aynı yükseklikte [count] kart. */
@Composable
fun CitiesSkeleton(modifier: Modifier = Modifier, count: Int = 4, cardHeight: Dp = 104.dp) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier.clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            SkeletonBlock(brush, Modifier.fillMaxWidth().height(cardHeight), RoundedCornerShape(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodaySkeletonPreview() {
    HavaTheme { TodaySkeleton() }
}
