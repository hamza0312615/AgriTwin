package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.WeatherAlert
import com.example.data.network.NasaPowerRepository
import com.example.ui.components.DemoDataBadge
import com.example.ui.components.HonestyStrip
import com.example.ui.components.RequestUrlDebugBanner
import com.example.ui.theme.SunlitBackground
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlatePrimary
import com.example.ui.theme.TextSlateSecondary
import kotlinx.coroutines.launch

data class FieldSoilProfile(
    val id: String,
    val name: String,
    val score: Int,
    val trend: String,
    val trendPositive: Boolean,
    val historyPoints: List<Int>,
    val organicMatter: Double,
    val phLevel: Double,
    val moisturePercent: Int
)

val MOCK_FIELDS = listOf(
    FieldSoilProfile(
        id = "north",
        name = "North Field",
        score = 78,
        trend = "+3.2",
        trendPositive = true,
        historyPoints = listOf(62, 65, 71, 68, 75, 72, 78),
        organicMatter = 3.4,
        phLevel = 6.8,
        moisturePercent = 42
    ),
    FieldSoilProfile(
        id = "south",
        name = "South Field",
        score = 82,
        trend = "+4.5",
        trendPositive = true,
        historyPoints = listOf(68, 70, 74, 73, 77, 80, 82),
        organicMatter = 3.9,
        phLevel = 7.1,
        moisturePercent = 48
    ),
    FieldSoilProfile(
        id = "vineyard",
        name = "Vineyard",
        score = 88,
        trend = "+1.8",
        trendPositive = true,
        historyPoints = listOf(80, 82, 84, 83, 85, 87, 88),
        organicMatter = 4.2,
        phLevel = 6.5,
        moisturePercent = 38
    ),
    FieldSoilProfile(
        id = "orchard",
        name = "Orchard",
        score = 69,
        trend = "-0.8",
        trendPositive = false,
        historyPoints = listOf(74, 73, 72, 70, 71, 70, 69),
        organicMatter = 2.8,
        phLevel = 7.4,
        moisturePercent = 31
    )
)

/**
 * Screen E: Alerts & Soil Health — Crafted to exactly match the target UI mockup.
 * Features:
 * - Top App Bar: Back arrow, "Alerts & Soil Health", Bell notification with badge, Profile avatar
 * - Coral/Terracotta Gradient Alert Card: Warning icon, "Extreme Heatwave 42°C", blue satellite "Live NASA POWER" pill, green recommendation pill, timestamp
 * - Soil Health Index Card: "78/100", "+3.2" green trend badge, smooth custom Canvas green sparkline with soft gradient fill
 * - "FIELDS" chip selector: North Field, South Field, Vineyard, Orchard
 * - Live NASA POWER daily fetch with visible debug trace and pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsSoilScreen(
    repository: NasaPowerRepository = remember { NasaPowerRepository() },
    onNavigateBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedFieldIndex by remember { mutableIntStateOf(0) }
    val currentField = MOCK_FIELDS[selectedFieldIndex]

    var weatherAlerts by remember { mutableStateOf<List<WeatherAlert>>(emptyList()) }
    var requestUrl by remember { mutableStateOf("") }
    var isLoadingAlerts by remember { mutableStateOf(false) }
    var alertsError by remember { mutableStateOf<String?>(null) }
    var showFormulaInfo by remember { mutableStateOf(false) }
    var showAllAlertsList by remember { mutableStateOf(false) }

    fun loadAlerts() {
        coroutineScope.launch {
            isLoadingAlerts = true
            alertsError = null

            val result = repository.fetchDailyDataWithResult(31.4187, 73.0791, daysCount = 30)
            if (result.isSuccess && result.getOrThrow().records.isNotEmpty()) {
                val data = result.getOrThrow()
                requestUrl = data.requestUrl
                weatherAlerts = repository.deriveWeatherAlerts(data.records, data.requestUrl)
                alertsError = null
            } else {
                alertsError = result.exceptionOrNull()?.message ?: "NASA POWER Daily API unreachable"
                weatherAlerts = repository.deriveWeatherAlerts(emptyList())
            }
            isLoadingAlerts = false
        }
    }

    LaunchedEffect(Unit) {
        loadAlerts()
    }

    PullToRefreshBox(
        isRefreshing = isLoadingAlerts,
        onRefresh = { loadAlerts() },
        modifier = Modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_alerts")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SunlitBackground)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("screen_alerts_soil")
        ) {
            // 1. TOP HEADER (Exact mockup layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSlatePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alerts & Soil Health",
                        color = TextSlatePrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notification Bell with green indicator dot
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextSlatePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // User Profile Avatar icon
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF16A34A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. CORAL/TERRACOTTA GRADIENT ALERT CARD (Exact mockup layout)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("card_heatwave_alert"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE26B54), // Warm coral/peach top-left
                                    Color(0xFFD65840)  // Deep terracotta bottom-right
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular Warning Icon inside translucent circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Extreme Heatwave 42°C",
                                    color = Color(0xFF0F172A),
                                    fontSize = 17.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Blue Pill: Satellite icon + "Live NASA POWER"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2563EB))
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SatelliteAlt,
                                        contentDescription = "Satellite",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Live NASA POWER",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.2.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Green Pill/Bullet: Leaf icon + Action recommendation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = "Leaf",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shade crops, increase irrigation. Expected until Aug 14.",
                                color = Color(0xFF14532D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer timestamp
                        Text(
                            text = "Updated 12 min ago",
                            color = Color(0xFF431407).copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. SOIL HEALTH INDEX CARD (Exact mockup layout)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_soil_health_index"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header: "Soil Health Index" + info icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soil Health Index",
                            color = TextSlatePrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { showFormulaInfo = !showFormulaInfo },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Big Score: "78/100" + green trend badge "+3.2"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${currentField.score}/100",
                            color = TextSlatePrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )

                        // Trend pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Trend",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currentField.trend,
                                color = Color(0xFF16A34A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smooth Vector Area Sparkline Chart (matching mockup curve and green fill)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        SoilSparklineCanvas(
                            points = currentField.historyPoints,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // "FIELDS" uppercase sub-caption
                    Text(
                        text = "FIELDS",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. FIELD CHIPS SELECTOR (Exact mockup layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MOCK_FIELDS.forEachIndexed { index, field ->
                    val isSelected = index == selectedFieldIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF16A34A) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF16A34A) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedFieldIndex = index }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("chip_field_${field.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSelected) "✓ ${field.name}" else field.name,
                            color = if (isSelected) Color.White else Color(0xFF334155),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Info Modal / Card if Info icon tapped
            AnimatedVisibility(visible = showFormulaInfo) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Soil Health Scoring Formula (NASA Spec):",
                            color = TextSlatePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Score = 0.35 × Organic Matter Index + 0.30 × Soil Moisture Saturation + 0.20 × pH Balance (Optimum 6.8) + 0.15 × NASA Thermal Canopy Moderation.",
                            color = TextSlateSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${currentField.name}: Organic Matter ${currentField.organicMatter}% · pH ${currentField.phLevel} · Moisture ${currentField.moisturePercent}%",
                            color = Color(0xFF16A34A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. NASA POWER Live Trace Debug Banner (Required by Part 1)
            if (requestUrl.isNotBlank()) {
                RequestUrlDebugBanner(url = requestUrl)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Honesty Strip
            HonestyStrip()

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable full list of all 30-day NASA alerts and demo storm events
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable { showAllAlertsList = !showAllAlertsList }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllAlertsList) "Hide Full 30-Day NASA Alert Log" else "View All NASA Hazard & Storm Events (${weatherAlerts.size})",
                    color = Color(0xFF0284C7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showAllAlertsList) "▲" else "▼",
                    color = Color(0xFF0284C7),
                    fontSize = 11.sp
                )
            }

            if (showAllAlertsList) {
                Spacer(modifier = Modifier.height(10.dp))
                weatherAlerts.forEach { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(alert.title, color = TextSlatePrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (alert.isLiveNasaDerived) {
                                    Text("Live NASA", color = Color(0xFF0284C7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    DemoDataBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.description, color = TextSlateSecondary, fontSize = 11.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Action: ${alert.suggestedAction}", color = Color(0xFF16A34A), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Custom smooth Canvas Sparkline matching the exact design in the mockup:
 * Continuous thick green bezier stroke (#15803D) with gentle pale green area gradient fill (#DCFCE7).
 */
@Composable
fun SoilSparklineCanvas(
    points: List<Int>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val minVal = (points.minOrNull() ?: 50) - 5
        val maxVal = (points.maxOrNull() ?: 90) + 5
        val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)

        val offsets = points.mapIndexed { i, pt ->
            val x = i * (w / (points.size - 1).coerceAtLeast(1))
            val normY = (pt - minVal) / range
            val y = h - (normY * (h * 0.72f) + (h * 0.14f))
            Offset(x, y)
        }

        if (offsets.size > 1) {
            // 1. Draw smooth area fill underneath
            val fillPath = Path().apply {
                moveTo(offsets.first().x, h)
                lineTo(offsets.first().x, offsets.first().y)
                for (i in 1 until offsets.size) {
                    val prev = offsets[i - 1]
                    val curr = offsets[i]
                    val cx = (prev.x + curr.x) / 2f
                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                }
                lineTo(offsets.last().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF86EFAC).copy(alpha = 0.55f), // Pale light green
                        Color(0xFFDCFCE7).copy(alpha = 0.25f)
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Draw smooth stroke curve
            val strokePath = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                for (i in 1 until offsets.size) {
                    val prev = offsets[i - 1]
                    val curr = offsets[i]
                    val cx = (prev.x + curr.x) / 2f
                    cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                }
            }

            drawPath(
                path = strokePath,
                color = Color(0xFF15803D), // Strong forest green matching mockup
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
