package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.DemoBadgeAmber
import com.example.ui.theme.DemoBadgeAmberBg
import com.example.ui.theme.DemoBadgeAmberBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueText
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldContainer
import com.example.ui.theme.SunGoldText
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlateSecondary

/**
 * NASA POWER Live Badge — unclipped single-line pill.
 */
@Composable
fun NasaLiveBadge(
    modifier: Modifier = Modifier,
    text: String = "Live · NASA POWER"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(SkyBlueContainer)
            .border(1.dp, SkyBlue.copy(alpha = 0.5f), RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 3.5.dp)
            .testTag("badge_nasa_live")
    ) {
        Box(
            modifier = Modifier
                .size(6.5.dp)
                .clip(CircleShape)
                .background(SkyBlue)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = SkyBlueText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * NASA GIBS Live Badge for Satellite imagery.
 */
@Composable
fun NasaGibsLiveBadge(
    modifier: Modifier = Modifier,
    text: String = "Live · NASA GIBS"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFFE0F2FE))
            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f), RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 3.5.dp)
            .testTag("badge_nasa_gibs")
    ) {
        Box(
            modifier = Modifier
                .size(6.5.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7))
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = Color(0xFF0369A1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * Demo Data Badge — amber pill designating synthetic benchmarks.
 */
@Composable
fun DemoDataBadge(
    modifier: Modifier = Modifier,
    text: String = "Demo data"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(DemoBadgeAmberBg)
            .border(1.dp, DemoBadgeAmberBorder, RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 3.5.dp)
            .testTag("badge_demo_data")
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(DemoBadgeAmber)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            color = DemoBadgeAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            letterSpacing = 0.1.sp
        )
    }
}

/**
 * Subtle Offline Status Indicator for Top Bar when connection or NASA calls fail.
 */
@Composable
fun OfflineStatusChip(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = isOffline) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AlertRedBg)
                .border(1.dp, AlertRedBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .testTag("indicator_offline")
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Offline indicator",
                tint = AlertRed,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Offline",
                color = AlertRed,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Visible NASA Request URL Debug Ribbon (Required by Part 1).
 */
@Composable
fun RequestUrlDebugBanner(
    url: String,
    modifier: Modifier = Modifier
) {
    if (url.isBlank()) return

    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, SunlitBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable { isExpanded = !isExpanded }
            .testTag("debug_url_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "Debug URL",
                tint = SkyBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NASA Request URL (Live Trace)",
                color = SkyBlueText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isExpanded) "Hide" else "Show URL",
                color = TextSlateMuted,
                fontSize = 10.sp
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = url,
                color = TextSlateSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Honesty Strip: Transparent indicator across screens.
 */
@Composable
fun HonestyStrip(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, SunlitBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("honesty_strip")
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Data Honesty Information",
            tint = SkyBlue,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Live NASA data where marked · Demo data clearly labelled",
            color = TextSlateSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp
        )
    }
}

/**
 * Disease Demo Banner: Mandatory compliance banner for Crop Disease Analyser.
 */
@Composable
fun DiseaseDemoBanner(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SunGoldContainer)
            .border(1.dp, SunGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("disease_demo_banner")
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Demo Mode Disclaimer",
            tint = SunGoldText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Demo mode — not a real diagnosis",
            color = SunGoldText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        )
    }
}

/**
 * Shimmer Loading Skeleton Box for clean async loading states.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFE2E8F0).copy(alpha = alpha))
    )
}
