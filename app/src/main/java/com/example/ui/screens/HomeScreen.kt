package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.data.model.ClimatologyData
import com.example.data.network.NasaPowerRepository
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HonestyStrip
import com.example.ui.components.NasaLiveBadge
import com.example.ui.components.RequestUrlDebugBanner
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryBright
import com.example.ui.theme.AgriPrimaryContainer
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.AgriPrimaryText
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueText
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldContainer
import com.example.ui.theme.SunGoldText
import com.example.ui.theme.SunlitBackground
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.SunlitSurfaceCard
import com.example.ui.theme.SunlitSurfaceSubtle
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlatePrimary
import com.example.ui.theme.TextSlateSecondary

/**
 * Landing Page (Screen A): Sunlit Agriculture Theme (v2).
 * Hero banner with custom Canvas vector illustration (satellite over green fields with sun & orbit ring),
 * Team DevTech credit, NASA Space Apps story, 3-step workflow, and feature navigators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFarm: () -> Unit,
    onNavigateToTwin: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToDisease: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    repository: NasaPowerRepository = remember { NasaPowerRepository() }
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var liveTelemetry by remember { mutableStateOf<ClimatologyData?>(null) }
    var refreshTimestamp by remember { mutableStateOf<String?>("Live") }

    fun refreshData() {
        coroutineScope.launch {
            isRefreshing = true
            val res = repository.fetchClimatology(31.4187, 73.0791)
            if (res.isSuccess) {
                liveTelemetry = res.getOrThrow()
                refreshTimestamp = "Live · Updated"
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { refreshData() },
        modifier = Modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_home")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SunlitBackground)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("screen_home")
        ) {
            // Live Status Notification Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SkyBlueContainer)
                    .border(1.dp, SkyBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SkyBlue))
                    Spacer(modifier = Modifier.width(6.dp))
                    val telem = liveTelemetry
                    if (telem != null) {
                        Text(
                            text = "Faisalabad: ${String.format(Locale.UK, "%.1f°C", telem.meanAnnualTempC)} · ${String.format(Locale.UK, "%.0fmm", telem.meanAnnualPrecipitationMm)}",
                            color = SkyBlueText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "NASA POWER Earth Observation Telemetry",
                            color = SkyBlueText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "Pull to refresh",
                    color = SkyBlueText.copy(alpha = 0.8f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Visible NASA Request URL Debug Ribbon (Required by Part 1)
            liveTelemetry?.requestUrl?.let { url ->
                if (url.isNotBlank()) {
                    RequestUrlDebugBanner(url = url, modifier = Modifier.padding(bottom = 8.dp))
                }
            }

        // Space Apps & Team Credit Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(AgriPrimaryFresh)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TEAM DEVTECH",
                    color = AgriPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
            }
            NasaLiveBadge(text = "NASA Space Apps 2026")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Card with Pure Canvas Vector Illustration (Satellite over green fields with sun and orbit ring)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SunlitBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Vector Canvas Illustration (Satellite orbiting over rolling green crop fields)
                SunlitHeroIllustration(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                // Hero Copy Block
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AgriPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "AgriTwin Earth Orbit",
                                tint = AgriPrimaryFresh,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AgriTwin",
                                color = TextSlatePrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Climate-Twin Farming Intelligence",
                                color = SkyBlue,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Accelerating climate volatility is shifting regional crop seasons faster than generational wisdom can adapt. AgriTwin harnesses NASA Earth observation climatology to find the places on Earth whose current climate matches your farm's projected future. By locating your climate twin, you discover resilient crop rotations, water regimes, and soil safeguards already succeeding there today.",
                        color = TextSlateSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SkyBlueContainer)
                            .border(1.dp, SkyBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "NASA Challenge: Field Shift — Adapting Farms with NASA Data",
                            color = SkyBlue,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visible Honesty Strip
        HonestyStrip()

        Spacer(modifier = Modifier.height(20.dp))

        // "How It Works" Section
        Text(
            text = "How It Works",
            color = TextSlatePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Connecting NASA spaceborne observation to on-farm decision making",
            color = TextSlateMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        HowItWorksStep(
            stepNumber = "1",
            title = "Twin Your Farm (Connect fields via satellite)",
            description = "Pin your land on NASA GIBS satellite imagery and discover global climate twins where your projected future climate is already flourishing today."
        )

        Spacer(modifier = Modifier.height(8.dp))

        HowItWorksStep(
            stepNumber = "2",
            title = "Monitor Climate & Conditions",
            description = "Track live NASA POWER daily rainfall, heatwave spikes, gale winds, and rootzone moisture balance with instant hazard threshold detection."
        )

        Spacer(modifier = Modifier.height(8.dp))

        HowItWorksStep(
            stepNumber = "3",
            title = "Act with Recommendations",
            description = "Deploy battle-tested crop rotations, actionable irrigation directives, and on-demand weekly satellite agronomic reports before yield failure strikes."
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Feature Launch Cards (Now includes My Farm)
        Text(
            text = "Intelligence Modules",
            color = TextSlatePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a core tool to explore live telemetry and models",
            color = TextSlateMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureLaunchCard(
            title = "My Farm (Satellite & Report)",
            subtitle = "NASA GIBS true-colour imagery + 7-day telemetry report",
            tag = "Live GIBS & POWER",
            isLive = true,
            icon = Icons.Default.Terrain,
            iconTint = AgriPrimaryFresh,
            iconBg = AgriPrimaryContainer,
            onClick = onNavigateToFarm,
            testTag = "card_nav_my_farm"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureLaunchCard(
            title = "Climate-Twin Matcher",
            subtitle = "NASA POWER climatology + normalized regional matching",
            tag = "Live NASA data",
            isLive = true,
            icon = Icons.Default.Public,
            iconTint = AgriPrimaryFresh,
            iconBg = AgriPrimaryContainer,
            onClick = onNavigateToTwin,
            testTag = "card_nav_twin"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureLaunchCard(
            title = "Water Dashboard",
            subtitle = "14-day rainfall time-series + AQUORA satellite pipeline",
            tag = "Live & Demo",
            isLive = true,
            icon = Icons.Default.WaterDrop,
            iconTint = SkyBlue,
            iconBg = SkyBlueContainer,
            onClick = onNavigateToWater,
            testTag = "card_nav_water"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureLaunchCard(
            title = "Crop Disease Analyser",
            subtitle = "On-device HSV foliar pigmentation & treatment playbook",
            tag = "Demo assessment",
            isLive = false,
            icon = Icons.Default.Biotech,
            iconTint = SunGoldText,
            iconBg = SunGoldContainer,
            onClick = onNavigateToDisease,
            testTag = "card_nav_disease"
        )

        Spacer(modifier = Modifier.height(10.dp))

        FeatureLaunchCard(
            title = "Storm Alerts & Soil Health",
            subtitle = "30-day NASA hazard triggers + 0–100 Soil Health Index",
            tag = "Live & Demo",
            isLive = true,
            icon = Icons.Default.Grass,
            iconTint = AgriPrimary,
            iconBg = AgriPrimaryContainer,
            onClick = onNavigateToAlerts,
            testTag = "card_nav_alerts"
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Professional Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AgriTwin · Developed by Team DevTech",
                color = TextSlateSecondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NASA Space Apps Challenge 2026 · Field Shift",
                color = TextSlateMuted,
                fontSize = 11.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Data source: NASA Langley Research Center (LaRC) POWER Project",
                color = TextSlateMuted,
                fontSize = 10.5.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
}

/**
 * Pure Compose Canvas Illustration:
 * Satellite orbiting over green terraced agricultural fields under radiant morning sun.
 */
@Composable
private fun SunlitHeroIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Sky Gradient: Soft morning azure to horizon mint
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBAE6FD), // Sky blue
                    Color(0xFFE0F2FE), // Soft cyan
                    Color(0xFFF0FDF4)  // Horizon tint
                ),
                startY = 0f,
                endY = h
            )
        )

        // 2. Radiant Morning Sun (Top right)
        val sunCenter = Offset(w * 0.82f, h * 0.32f)
        val sunRadius = 26f

        // Corona glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDE68A).copy(alpha = 0.7f), Color.Transparent),
                center = sunCenter,
                radius = 70f
            ),
            center = sunCenter,
            radius = 70f
        )
        // Core sun disc
        drawCircle(
            color = Color(0xFFF59E0B),
            center = sunCenter,
            radius = sunRadius
        )

        // 3. Elliptical Orbital Ring (Crossing from top left toward sun)
        val orbitPath = Path().apply {
            moveTo(w * 0.05f, h * 0.55f)
            cubicTo(
                w * 0.25f, h * 0.10f,
                w * 0.70f, h * 0.12f,
                w * 0.95f, h * 0.40f
            )
        }
        drawPath(
            path = orbitPath,
            color = Color(0xFF0284C7).copy(alpha = 0.45f),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // 4. Satellite at orbit apex (~ (w * 0.38f, h * 0.22f))
        val satCenter = Offset(w * 0.38f, h * 0.23f)

        // Solar panels (left and right blue rectangles)
        drawRect(
            color = Color(0xFF0369A1),
            topLeft = Offset(satCenter.x - 22f, satCenter.y - 5f),
            size = Size(10f, 10f)
        )
        drawRect(
            color = Color(0xFF0369A1),
            topLeft = Offset(satCenter.x + 12f, satCenter.y - 5f),
            size = Size(10f, 10f)
        )
        // Satellite bus (golden center box)
        drawRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(satCenter.x - 5f, satCenter.y - 6f),
            size = Size(10f, 12f)
        )
        // Telemetry beacon dot
        drawCircle(
            color = Color(0xFF22C55E),
            center = Offset(satCenter.x, satCenter.y - 8f),
            radius = 2.5f
        )

        // 5. Terraced Green Crop Fields (Rolling layered hills)
        // Back ridge (medium forest green)
        val backHill = Path().apply {
            moveTo(0f, h * 0.65f)
            cubicTo(w * 0.35f, h * 0.52f, w * 0.75f, h * 0.72f, w, h * 0.58f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = backHill, color = Color(0xFF15803D))

        // Mid ridge (fresh agricultural green)
        val midHill = Path().apply {
            moveTo(0f, h * 0.78f)
            cubicTo(w * 0.28f, h * 0.66f, w * 0.65f, h * 0.85f, w, h * 0.70f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = midHill, color = Color(0xFF16A34A))

        // Foreground field (vibrant crop rows)
        val frontHill = Path().apply {
            moveTo(0f, h * 0.90f)
            cubicTo(w * 0.40f, h * 0.82f, w * 0.70f, h * 0.94f, w, h * 0.84f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = frontHill, color = Color(0xFF22C55E))

        // Subtle furrow lines
        for (i in 1..4) {
            val fx = w * (0.15f * i)
            drawLine(
                color = Color(0xFF14532D).copy(alpha = 0.35f),
                start = Offset(fx, h * 0.88f),
                end = Offset(fx + 25f, h),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
private fun HowItWorksStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SunlitSurfaceCard)
            .border(1.dp, SunlitBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgriPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = AgriPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = TextSlatePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                color = TextSlateSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun FeatureLaunchCard(
    title: String,
    subtitle: String,
    tag: String,
    isLive: Boolean,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPressed) 0.985f else 1f)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .border(1.dp, SunlitBorder, RoundedCornerShape(16.dp))
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextSlatePrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSlateMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate to $title",
                tint = TextSlateMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
