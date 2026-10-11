package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.DiseaseRiskLevel
import com.example.util.DiseaseAlertNotificationManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.DEFAULT_FARMS
import com.example.data.model.SavedFarm
import com.example.data.model.WeeklySatelliteReport
import com.example.data.network.NasaPowerRepository
import com.example.ui.components.DemoDataBadge
import com.example.ui.components.HonestyStrip
import com.example.ui.components.NasaGibsLiveBadge
import com.example.ui.components.NasaLiveBadge
import com.example.ui.components.RequestUrlDebugBanner
import com.example.ui.components.SkeletonBox
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryContainer
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.AgriPrimaryText
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.DemoBadgeAmber
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
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Screen F: My Farm (Headline v3 feature).
 * Observes farmer's land using NASA GIBS true-colour imagery and generates Weekly Satellite Reports from real NASA POWER daily telemetry.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyFarmScreen(
    repository: NasaPowerRepository = remember { NasaPowerRepository() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Notification permission launcher for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(DiseaseAlertNotificationManager.hasNotificationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    // Local notification feedback message
    var notificationAlertMessage by remember { mutableStateOf<String?>(null) }

    // Multiple saved farms state
    var savedFarms by remember { mutableStateOf(DEFAULT_FARMS) }
    var selectedFarm by remember { mutableStateOf(DEFAULT_FARMS.first()) }
    var showAddFarmDialog by remember { mutableStateOf(false) }

    // Date stepper for NASA GIBS satellite passes (recent 7 days)
    val gibsDates = remember {
        listOf(
            "2026-10-08",
            "2026-10-07",
            "2026-10-06",
            "2026-10-05",
            "2026-10-04",
            "2026-10-03",
            "2026-10-02"
        )
    }
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    val currentGibsDate = gibsDates[selectedDateIndex]

    // Weekly Report State
    var weeklyReport by remember { mutableStateOf<WeeklySatelliteReport?>(null) }
    var isGeneratingReport by remember { mutableStateOf(false) }
    var reportError by remember { mutableStateOf<String?>(null) }
    var isFullReportExpanded by remember { mutableStateOf(false) }

    // Add Farm Form State
    var newFarmName by remember { mutableStateOf("") }
    var newFarmCrop by remember { mutableStateOf("Wheat") }
    var newFarmLat by remember { mutableStateOf("31.4200") }
    var newFarmLon by remember { mutableStateOf("73.1000") }
    val cropOptions = listOf("Wheat", "Cotton", "Rice", "Maize", "Sugarcane")

    fun generateReportForFarm(farm: SavedFarm) {
        coroutineScope.launch {
            isGeneratingReport = true
            reportError = null

            val result = repository.generateWeeklyReport(farm)
            if (result.isSuccess) {
                val report = result.getOrThrow()
                weeklyReport = report
                reportError = null

                // Check disease risk transition and alert if escalated to HIGH
                val transition = DiseaseAlertNotificationManager.checkAndNotifyDiseaseRiskChange(
                    context = context,
                    farm = farm,
                    report = report
                )
                if (transition.escalatedToHigh) {
                    notificationAlertMessage = transition.message
                }
            } else {
                reportError = result.exceptionOrNull()?.message ?: "NASA POWER Daily API error"
            }
            isGeneratingReport = false
        }
    }

    // Initialize notification channel and generate initial report
    LaunchedEffect(Unit) {
        DiseaseAlertNotificationManager.createNotificationChannel(context)
    }

    LaunchedEffect(selectedFarm) {
        generateReportForFarm(selectedFarm)
    }

    PullToRefreshBox(
        isRefreshing = isGeneratingReport,
        onRefresh = { generateReportForFarm(selectedFarm) },
        modifier = Modifier
            .fillMaxSize()
            .testTag("pull_to_refresh_my_farm")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SunlitBackground)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .testTag("screen_my_farm")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Farm",
                        color = TextSlatePrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "NASA GIBS Satellite Observation & Weekly Soil-Crop Report",
                        color = TextSlateMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                NasaGibsLiveBadge()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Honesty Strip
            HonestyStrip()

            Spacer(modifier = Modifier.height(14.dp))

            // Farm Selector & Add Farm Button
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terrain, contentDescription = "Farms", tint = AgriPrimaryFresh, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Saved Farm Fields", color = TextSlatePrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddFarmDialog = !showAddFarmDialog },
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("button_open_add_farm")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = AgriPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Farm", color = AgriPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        savedFarms.forEach { farm ->
                            val isSelected = farm.id == selectedFarm.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFarm = farm },
                                label = {
                                    Text(
                                        text = "${farm.name} (${farm.crop})",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgriPrimaryContainer,
                                    selectedLabelColor = AgriPrimaryText,
                                    containerColor = SunlitSurfaceSubtle,
                                    labelColor = TextSlateSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = SunlitBorder,
                                    selectedBorderColor = AgriPrimaryFresh
                                ),
                                modifier = Modifier.testTag("chip_farm_${farm.id}")
                            )
                        }
                    }
                }
            }

            // Expandable "Add Farm" Flow
            AnimatedVisibility(visible = showAddFarmDialog) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimaryFresh.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Setup New Farm Parcel", color = TextSlatePrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showAddFarmDialog = false }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSlateMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newFarmName,
                            onValueChange = { newFarmName = it },
                            label = { Text("Farm / Field Name", fontSize = 11.5.sp) },
                            placeholder = { Text("e.g. Chak 88 Chenab North", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("input_farm_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AgriPrimaryFresh,
                                unfocusedBorderColor = SunlitBorder,
                                focusedTextColor = TextSlatePrimary,
                                unfocusedTextColor = TextSlatePrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Primary Crop:", color = TextSlateSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            cropOptions.forEach { crop ->
                                val isSelected = newFarmCrop == crop
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { newFarmCrop = crop },
                                    label = { Text(crop, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SkyBlueContainer,
                                        selectedLabelColor = SkyBlueText,
                                        containerColor = SunlitSurfaceSubtle,
                                        labelColor = TextSlateSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = SunlitBorder,
                                        selectedBorderColor = SkyBlue
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newFarmLat,
                                onValueChange = { newFarmLat = it },
                                label = { Text("Latitude", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("input_farm_lat"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AgriPrimaryFresh,
                                    unfocusedBorderColor = SunlitBorder,
                                    focusedTextColor = TextSlatePrimary,
                                    unfocusedTextColor = TextSlatePrimary
                                )
                            )
                            OutlinedTextField(
                                value = newFarmLon,
                                onValueChange = { newFarmLon = it },
                                label = { Text("Longitude", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("input_farm_lon"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AgriPrimaryFresh,
                                    unfocusedBorderColor = SunlitBorder,
                                    focusedTextColor = TextSlatePrimary,
                                    unfocusedTextColor = TextSlatePrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val lat = newFarmLat.toDoubleOrNull() ?: 31.42
                                val lon = newFarmLon.toDoubleOrNull() ?: 73.10
                                val name = newFarmName.ifBlank { "Farm Plot ${savedFarms.size + 1}" }
                                val newFarm = SavedFarm(
                                    id = "farm_${System.currentTimeMillis()}",
                                    name = name,
                                    crop = newFarmCrop,
                                    latitude = lat,
                                    longitude = lon
                                )
                                savedFarms = savedFarms + newFarm
                                selectedFarm = newFarm
                                showAddFarmDialog = false
                                newFarmName = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("button_save_farm"),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryFresh),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Farm & Connect Satellite", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Satellite Observation Section (NASA GIBS True-Colour Imagery)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Satellite Land Observation",
                                color = TextSlatePrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedFarm.name} · (${selectedFarm.latitude}, ${selectedFarm.longitude})",
                                color = TextSlateMuted,
                                fontSize = 11.5.sp
                            )
                        }
                        NasaGibsLiveBadge()
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Date Stepper for satellite passes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SunlitSurfaceSubtle)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedDateIndex < gibsDates.size - 1) selectedDateIndex++
                            },
                            enabled = selectedDateIndex < gibsDates.size - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Earlier pass", tint = TextSlateSecondary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Pass date", tint = SkyBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODIS Pass: $currentGibsDate",
                                color = TextSlatePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedDateIndex > 0) selectedDateIndex--
                            },
                            enabled = selectedDateIndex > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Later pass", tint = TextSlateSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Leaflet Map with NASA GIBS Tile Layer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SunlitBorder, RoundedCornerShape(12.dp))
                    ) {
                        GibsLeafletMapView(
                            latitude = selectedFarm.latitude,
                            longitude = selectedFarm.longitude,
                            date = currentGibsDate,
                            farmName = selectedFarm.name,
                            onLocationPicked = { lat, lon ->
                                newFarmLat = String.format(Locale.US, "%.4f", lat)
                                newFarmLon = String.format(Locale.US, "%.4f", lon)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Layer: NASA GIBS MODIS Terra Corrected Reflectance True-Color (WMTS 250m resolution). Tap map to pick coordinates.",
                        color = TextSlateMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Weekly Satellite Report Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weekly Satellite Report",
                        color = TextSlatePrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Last 7 days of verified NASA POWER telemetry for ${selectedFarm.name}",
                        color = TextSlateMuted,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val report = weeklyReport
                if (report != null && report.isLiveReport) {
                    NasaLiveBadge()
                } else {
                    DemoDataBadge()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Error display if report generation fails
            AnimatedVisibility(visible = reportError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlertRedBg)
                        .border(1.dp, AlertRedBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "Error", tint = AlertRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Weekly Report Generation Notice", color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(reportError ?: "Connection issue", color = AlertRed, fontSize = 11.5.sp)
                    }
                }
            }

            // Local Notification System Feedback Banner
            AnimatedVisibility(visible = notificationAlertMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("banner_disease_notification_sent"),
                    colors = CardDefaults.cardColors(containerColor = SkyBlueContainer),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SkyBlue.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alert",
                            tint = SkyBlueText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = notificationAlertMessage ?: "",
                            color = SkyBlueText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            lineHeight = 16.sp
                        )
                        IconButton(
                            onClick = { notificationAlertMessage = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SkyBlueText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            val report = weeklyReport
            if (isGeneratingReport && report == null) {
                Column {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(120.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            } else if (report != null) {
                // Request URL debug line
                RequestUrlDebugBanner(url = report.requestUrl)

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AgriPrimaryFresh.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .testTag("card_weekly_report"),
                    colors = CardDefaults.cardColors(containerColor = SunlitSurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Summary · ${report.farmName}",
                                color = TextSlatePrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            NasaLiveBadge()
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // KPI Matrix
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Rainfall box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SkyBlueContainer)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Total Rain (7d)", color = SkyBlueText, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.UK, "%.1f", report.totalRainfallMm)} mm",
                                        color = SkyBlueText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${if (report.deltaRainfallMm >= 0) "▲ +" else "▼ "}${String.format(Locale.UK, "%.1f", report.deltaRainfallMm)} mm vs prev",
                                        color = TextSlateSecondary,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }

                            // Temp box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SunGoldContainer)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("Avg / Max Temp", color = SunGoldText, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.UK, "%.1f", report.avgTempC)}° / ${String.format(Locale.UK, "%.1f", report.maxTempC)}°",
                                        color = SunGoldText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${report.heatStressDaysCount} heat days (≥40°C)",
                                        color = TextSlateSecondary,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vegetation health note
                        Row(verticalAlignment = Alignment.Top) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AgriPrimaryFresh).padding(top = 4.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Vegetation Health Trend:", color = AgriPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    DemoDataBadge(text = "Demo index")
                                }
                                Text(text = report.vegetationHealthTrend, color = TextSlateSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Irrigation Advice in one sentence
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Water", tint = SkyBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Irrigation Directive:", color = SkyBlueText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = report.irrigationAdvice, color = TextSlateSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Upgraded Disease Risk Section with Local Notification Alerts
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_disease_risk_panel"),
                            colors = CardDefaults.cardColors(
                                containerColor = when (report.diseaseRiskLevel) {
                                    DiseaseRiskLevel.HIGH -> AlertRedBg
                                    DiseaseRiskLevel.MODERATE -> SunGoldContainer.copy(alpha = 0.5f)
                                    DiseaseRiskLevel.LOW -> AgriPrimaryContainer.copy(alpha = 0.45f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (report.diseaseRiskLevel) {
                                    DiseaseRiskLevel.HIGH -> AlertRedBorder
                                    DiseaseRiskLevel.MODERATE -> SunGold
                                    DiseaseRiskLevel.LOW -> AgriPrimaryFresh.copy(alpha = 0.4f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (report.diseaseRiskLevel) {
                                                DiseaseRiskLevel.HIGH -> Icons.Default.Warning
                                                DiseaseRiskLevel.MODERATE -> Icons.Default.Warning
                                                DiseaseRiskLevel.LOW -> Icons.Default.CheckCircle
                                            },
                                            contentDescription = "Disease Risk",
                                            tint = when (report.diseaseRiskLevel) {
                                                DiseaseRiskLevel.HIGH -> AlertRed
                                                DiseaseRiskLevel.MODERATE -> SunGoldText
                                                DiseaseRiskLevel.LOW -> AgriPrimary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Atmospheric Disease Risk",
                                            color = TextSlatePrimary,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Risk level badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (report.diseaseRiskLevel) {
                                                    DiseaseRiskLevel.HIGH -> AlertRed
                                                    DiseaseRiskLevel.MODERATE -> SunGoldText
                                                    DiseaseRiskLevel.LOW -> AgriPrimaryFresh
                                                }
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${report.diseaseRiskLevel.label.uppercase(Locale.UK)} RISK",
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = report.diseaseRiskNote,
                                    color = TextSlateSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Permission prompt if needed on Android 13+
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Notifications, contentDescription = "Notify", tint = TextSlateMuted, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Enable push alerts to get notified when risk hits High",
                                                color = TextSlateMuted,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                        Button(
                                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryFresh),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Enable", fontSize = 10.5.sp, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // Interactive Testing & Baseline Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                            val sent = DiseaseAlertNotificationManager.simulateRiskEscalation(context, selectedFarm)
                                            notificationAlertMessage = if (sent) {
                                                "🔔 Local notification sent: Simulated Low → High shift for ${selectedFarm.name} (${selectedFarm.crop}). Check notification shade!"
                                            } else {
                                                "Simulated Low → High shift recorded. Enable notification permission to view system banner."
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(34.dp)
                                            .testTag("button_simulate_disease_alert"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Test Alert",
                                            tint = AlertRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Test Low→High Alert",
                                            color = AlertRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            DiseaseAlertNotificationManager.resetFarmRiskToLow(context, selectedFarm.id)
                                            notificationAlertMessage = "Baseline risk reset to LOW for ${selectedFarm.name}. Next rain event will trigger alert."
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                            .testTag("button_reset_disease_risk"),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TextSlateMuted.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "Reset to Low",
                                            color = TextSlateSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Expandable Day-by-Day Table
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SunlitSurfaceSubtle)
                                .clickable { isFullReportExpanded = !isFullReportExpanded }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFullReportExpanded) "Hide Day-by-Day Telemetry Table" else "View Full 7-Day Telemetry Table (${report.dailyRecords.size} days)",
                                color = SkyBlueText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isFullReportExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Table",
                                tint = SkyBlue
                            )
                        }

                        if (isFullReportExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, SunlitBorder, RoundedCornerShape(8.dp))
                            ) {
                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date", color = TextSlatePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Rain (mm)", color = SkyBlueText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Temp (°C)", color = SunGoldText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("Wind (m/s)", color = TextSlateSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                }

                                // Table Rows
                                report.dailyRecords.forEachIndexed { idx, day ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (idx % 2 == 0) Color.White else SunlitSurfaceSubtle)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(day.dateString, color = TextSlatePrimary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                        Text(String.format(Locale.UK, "%.1f", day.rainfallMm), color = SkyBlueText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Text(String.format(Locale.UK, "%.1f", day.maxTempC ?: 0.0), color = SunGoldText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Text(String.format(Locale.UK, "%.1f", day.windSpeedMs ?: 0.0), color = TextSlateSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Generate/Refresh button
            Button(
                onClick = { generateReportForFarm(selectedFarm) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("button_generate_weekly_report"),
                enabled = !isGeneratingReport,
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryFresh),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isGeneratingReport) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Querying NASA POWER Live Telemetry...", fontSize = 13.sp, color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Generate", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Fresh Weekly Report", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Interactive Leaflet Map View using NASA GIBS true-colour WMTS layer.
 * Zero API keys or proxies required.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GibsLeafletMapView(
    latitude: Double,
    longitude: Double,
    date: String,
    farmName: String,
    onLocationPicked: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val escapedFarmName = farmName.replace("'", "\\'")

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
          <style>
            html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #071728; }
            .leaflet-control-attribution { font-size: 8px !important; background: rgba(255,255,255,0.7) !important; }
            .farm-beacon {
              background-color: #22c55e;
              width: 14px;
              height: 14px;
              border-radius: 50%;
              border: 2px solid white;
              box-shadow: 0 0 10px #22c55e;
            }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var map = L.map('map', { zoomControl: true }).setView([$latitude, $longitude], 8);

            // NASA GIBS MODIS Terra TrueColor WMTS Tile Layer (Keyless)
            var gibsUrl = 'https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/MODIS_Terra_CorrectedReflectance_TrueColor/default/$date/GoogleMapsCompatible_Level9/{z}/{y}/{x}.jpg';
            
            L.tileLayer(gibsUrl, {
              maxZoom: 9,
              attribution: 'NASA Earthdata / GIBS &copy; OpenStreetMap'
            }).addTo(map);

            // Farm Boundary circle
            var farmCircle = L.circle([$latitude, $longitude], {
              color: '#16a34a',
              fillColor: '#22c55e',
              fillOpacity: 0.35,
              radius: 2500
            }).addTo(map);

            // Farm pin marker
            var marker = L.marker([$latitude, $longitude]).addTo(map);
            marker.bindPopup("<b>$escapedFarmName</b><br>Lat: $latitude, Lon: $longitude").openPopup();

            // Click listener to pick coordinates
            map.on('click', function(e) {
              marker.setLatLng(e.latlng);
              farmCircle.setLatLng(e.latlng);
              marker.bindPopup("<b>Selected Coordinate</b><br>" + e.latlng.lat.toFixed(4) + ", " + e.latlng.lng.toFixed(4)).openPopup();
              if (window.AndroidInterface && window.AndroidInterface.onLocationPicked) {
                window.AndroidInterface.onLocationPicked(e.latlng.lat, e.latlng.lng);
              }
            });
          </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onLocationPicked(lat: Double, lon: Double) {
                        onLocationPicked(lat, lon)
                    }
                }, "AndroidInterface")
                loadDataWithBaseURL("https://gibs.earthdata.nasa.gov", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://gibs.earthdata.nasa.gov", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier.fillMaxSize().testTag("view_gibs_map")
    )
}
