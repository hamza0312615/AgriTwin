package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.aquora.AquoraAdapter
import com.example.data.aquora.IrrigationAdvice
import com.example.data.model.DailyRainfallRecord
import com.example.data.model.FieldWaterStatus
import com.example.data.network.NasaPowerRepository
import com.example.ui.components.DemoDataBadge
import com.example.ui.components.HonestyStrip
import com.example.ui.components.NasaLiveBadge
import com.example.ui.components.RequestUrlDebugBanner
import com.example.ui.components.SkeletonBox
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryContainer
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.DemoBadgeAmber
import com.example.ui.theme.DemoBadgeAmberBg
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueText
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldContainer
import com.example.ui.theme.SunlitBackground
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.SunlitSurfaceCard
import com.example.ui.theme.SunlitSurfaceSubtle
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlatePrimary
import com.example.ui.theme.TextSlateSecondary
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Screen C: Water Dashboard (/water) — Sunlit Light Theme (v2).
 * Displays REAL 14-day rainfall fetched live from NASA POWER daily API (parameters T2M, PRECTOTCORR, WS2M),
 * plus AQUORA satellite adapter integration and soil moisture field cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterDashboardScreen(
    repository: NasaPowerRepository = remember { NasaPowerRepository() },
    aquoraAdapter: AquoraAdapter = remember { AquoraAdapter() }
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var dailyRecords by remember { mutableStateOf<List<DailyRainfallRecord>>(emptyList()) }
    var dailyRequestUrl by remember { mutableStateOf("") }
    var fieldStatuses by remember { mutableStateOf<List<FieldWaterStatus>>(emptyList()) }
    var irrigationAdvice by remember { mutableStateOf<IrrigationAdvice?>(null) }
    var isLiveDailyData by remember { mutableStateOf(false) }
    var isLoadingDaily by remember { mutableStateOf(false) }
    var dailyError by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        coroutineScope.launch {
            isLoadingDaily = true
            dailyError = null

            // Query NASA POWER Daily API (parameters: T2M, PRECTOTCORR, WS2M) for 14-day window
            val result = repository.fetchDailyDataWithResult(31.4187, 73.0791, daysCount = 14)
            if (result.isSuccess && result.getOrThrow().records.isNotEmpty()) {
                val data = result.getOrThrow()
                dailyRecords = data.records
                dailyRequestUrl = data.requestUrl
                isLiveDailyData = true
                dailyError = null
            } else {
                dailyRecords = emptyList()
                isLiveDailyData = false
                dailyError = result.exceptionOrNull()?.message ?: "NASA POWER Daily API unreachable"
            }

            // Ingest AQUORA satellite pipeline telemetry (Field cards remain demo until AQUORA satellite connected)
            val fields = aquoraAdapter.getFieldWaterStatuses("faisalabad")
            fieldStatuses = fields
            val totalRain = dailyRecords.sumOf { it.rainfallMm }
            val hasRecentRain = totalRain >= 10.0
            irrigationAdvice = aquoraAdapter.synthesizeIrrigationAdvice(fields, totalRain, hasRecentRain)

            isLoadingDaily = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    PullToRefreshBox(
        isRefreshing = isLoadingDaily,
        onRefresh = { loadData() },
        modifier = Modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_water")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SunlitBackground)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .testTag("screen_water")
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Water Dashboard",
                    color = TextSlatePrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "NASA POWER Precipitation & AQUORA Satellite Hydrology",
                    color = TextSlateMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isLiveDailyData) {
                NasaLiveBadge()
            } else {
                DemoDataBadge(text = "Demo data")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Honesty Banner
        HonestyStrip()

        // Visible NASA Request URL Debug Ribbon (Required by Part 1)
        if (dailyRequestUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            RequestUrlDebugBanner(url = dailyRequestUrl)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actionable Irrigation Advice Card
        val advice = irrigationAdvice
        if (advice != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SkyBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .testTag("card_irrigation_advice"),
                colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SkyBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Water Advisory",
                                    tint = SkyBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Actionable Irrigation Advice",
                                color = SkyBlueText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DemoDataBadge()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = advice.headline,
                        color = TextSlatePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = advice.recommendationSummary,
                        color = TextSlateSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SkyBlueContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Estimated Conserved Water: ${advice.waterVolumeSavingCubicMetersPerHectare} m³/ha",
                            color = SkyBlueText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REAL 14-Day Rainfall Card (NASA POWER Daily API)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "14-Day Rainfall History (PRECTOTCORR)",
                            color = TextSlatePrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily surface precipitation in millimetres",
                            color = TextSlateMuted,
                            fontSize = 11.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isLiveDailyData) {
                        NasaLiveBadge()
                    } else {
                        DemoDataBadge()
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error State if API Fails
                AnimatedVisibility(visible = dailyError != null) {
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRedBg)
                                .border(1.dp, AlertRedBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "Error", tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "NASA POWER Daily API Error", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = dailyError ?: "Network connection issue", color = AlertRed, fontSize = 11.5.sp)
                            }
                        }
                    }
                }

                // Loading State vs Data Bars
                if (isLoadingDaily && dailyRecords.isEmpty()) {
                    Column {
                        SkeletonBox(modifier = Modifier.fillMaxWidth().height(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(14) {
                                SkeletonBox(modifier = Modifier.weight(1f).fillMaxHeight())
                            }
                        }
                    }
                } else if (dailyRecords.isNotEmpty()) {
                    val totalRainfall = dailyRecords.sumOf { it.rainfallMm }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SunlitSurfaceSubtle)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "14-Day Accumulated Rainfall:",
                            color = TextSlateSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.UK, "%.1f mm", totalRainfall),
                            color = SkyBlueText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val maxPrecip = (dailyRecords.maxOfOrNull { it.rainfallMm } ?: 1.0).coerceAtLeast(10.0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dailyRecords.takeLast(14).forEach { record ->
                            val barHeightFraction = (record.rainfallMm / maxPrecip).toFloat().coerceIn(0.05f, 1f)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (record.rainfallMm > 0) {
                                    Text(
                                        text = String.format(Locale.UK, "%.0f", record.rainfallMm),
                                        color = SkyBlueText,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(barHeightFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (record.rainfallMm > 10.0) AgriPrimaryFresh else SkyBlue
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = record.dateString.take(2),
                                    color = TextSlateMuted,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { loadData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("button_refresh_water"),
                    enabled = !isLoadingDaily,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoadingDaily) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SkyBlue, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = SkyBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Query NASA POWER Live Daily Rainfall", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Field Cards: NDWI / Soil Moisture (Demo Data until AQUORA pipeline connected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Field-Scale Water Status",
                    color = TextSlatePrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High-resolution rootzone telemetry & stress monitoring",
                    color = TextSlateMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            DemoDataBadge()
        }

        Spacer(modifier = Modifier.height(10.dp))

        fieldStatuses.forEach { field ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("card_field_${field.fieldId}"),
                colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = field.fieldName,
                                color = TextSlatePrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Crop: ${field.crop}",
                                color = AgriPrimaryFresh,
                                fontSize = 12.sp
                            )
                        }
                        DemoDataBadge()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soil Moisture: ${field.soilMoisturePercent}%",
                            color = TextSlatePrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val stressColor = when {
                            field.waterStressIndex >= 0.6 -> AlertRed
                            field.waterStressIndex >= 0.35 -> DemoBadgeAmber
                            else -> AgriPrimaryFresh
                        }
                        Text(
                            text = "Stress Index: ${field.waterStressIndex}",
                            color = stressColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { field.soilMoisturePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (field.soilMoisturePercent < 35) DemoBadgeAmber else SkyBlue,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Drainage: ${field.drainageStatus}",
                        color = TextSlateMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // AQUORA Pipeline Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SunlitSurfaceSubtle),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SatelliteAlt, contentDescription = "AQUORA", tint = SkyBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AQUORA Satellite Water Pipeline Integration",
                        color = SkyBlueText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Architectural Module: aquora-adapter.kt\nPlug-in point for Team DevTech's proprietary satellite pipeline connecting NASA SMAP microwave radiometry (L3/L4 soil moisture) and NASA ECOSTRESS evapotranspiration indices directly to on-farm decision systems.",
                    color = TextSlateSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
}
