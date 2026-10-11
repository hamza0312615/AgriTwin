package com.example.ui.screens

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import com.example.data.location.AppLocationManager
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.DiseaseRiskLevel
import com.example.util.AppLanguageState
import com.example.util.AppTtsManager
import com.example.util.DiseaseAlertNotificationManager
import com.example.util.ReportPdfExporter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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

    // Add Farm Form State
    var newFarmName by remember { mutableStateOf("") }
    var newFarmCrop by remember { mutableStateOf("Wheat") }
    var newFarmLat by remember { mutableStateOf("31.4200") }
    var newFarmLon by remember { mutableStateOf("73.1000") }
    val cropOptions = listOf("Wheat", "Cotton", "Rice", "Maize", "Sugarcane")

    // Location permission launcher for GPS autofill
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                val res = AppLocationManager.fetchDeviceLocation(context)
                if (res.isSuccess) {
                    val loc = res.getOrThrow()
                    newFarmLat = String.format(Locale.US, "%.4f", loc.latitude)
                    newFarmLon = String.format(Locale.US, "%.4f", loc.longitude)
                }
            }
        }
    }

    // Local notification feedback message
    var notificationAlertMessage by remember { mutableStateOf<String?>(null) }

    // Multiple saved farms state
    var savedFarms by remember { mutableStateOf(DEFAULT_FARMS) }
    var selectedFarm by remember { mutableStateOf(DEFAULT_FARMS.first()) }
    var showAddFarmDialog by remember { mutableStateOf(false) }

    // Date stepper for NASA GIBS satellite passes (dynamically generated from yesterday back 7 days)
    val gibsDates: List<String> = remember {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        (1..7).map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            dateFormat.format(cal.time)
        }
    }
    val yesterdayFallback = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
    var selectedDateIndex by remember { mutableIntStateOf(0) }
    val currentGibsDate: String = gibsDates.getOrElse(selectedDateIndex) { gibsDates.firstOrNull() ?: yesterdayFallback }

    val isUrdu = AppLanguageState.isUrdu
    val isSpeaking = AppTtsManager.isSpeaking

    fun speakReport(report: WeeklySatelliteReport) {
        val textEn = buildString {
            append("Weekly Satellite Report for ${report.farmName}. ")
            append("Crop parcel: ${selectedFarm.crop}. ")
            append("Seven-day rainfall is ${String.format(Locale.UK, "%.1f", report.totalRainfallMm)} millimetres, ")
            append("compared to ${String.format(Locale.UK, "%.1f", report.historicalTotalRainfallMm)} millimetres in ${report.historicalYearLabel}. ")
            append("Average temperature is ${String.format(Locale.UK, "%.1f", report.avgTempC)} degrees Celsius. ")
            append("Root zone soil moisture is ${String.format(Locale.UK, "%.1f", report.soilMoisturePercent)} percent, status is ${report.soilMoistureStatus}. ")
            append("Net irrigation requirement is ${String.format(Locale.UK, "%.0f", report.irrigationRequirementMm)} millimetres. ")
            append("Agronomic advice: ${report.irrigationAdvice}")
        }

        val statusUr = when {
            report.soilMoisturePercent < 22.0 -> "کمی کا شکار"
            report.soilMoisturePercent <= 42.0 -> "مناسب اور متوازن"
            else -> "سیراب اور زیادہ نمی"
        }
        val textUr = buildString {
            append("ہفتہ وار سیٹلائٹ رپورٹ برائے ${report.farmName}۔ ")
            append("فصل: ${selectedFarm.crop}۔ ")
            append("گزشتہ سات دنوں کی بارش ${String.format(Locale.UK, "%.1f", report.totalRainfallMm)} ملی میٹر رہی، ")
            append("جبکہ پچھلے سال ${report.historicalYearLabel} میں ${String.format(Locale.UK, "%.1f", report.historicalTotalRainfallMm)} ملی میٹر تھی۔ ")
            append("اوسط درجہ حرارت ${String.format(Locale.UK, "%.1f", report.avgTempC)} ڈگری سینٹی گریڈ ہے۔ ")
            append("زمین کی نمی ${String.format(Locale.UK, "%.1f", report.soilMoisturePercent)} فیصد ہے یعنی $statusUr۔ ")
            append("آبپاشی کی ضرورت ${String.format(Locale.UK, "%.0f", report.irrigationRequirementMm)} ملی میٹر ہے۔ ")
            append("زرعی مشورہ: ${report.irrigationAdvice}")
        }

        AppTtsManager.speak(
            context = context,
            textEn = textEn,
            textUr = textUr,
            isUrdu = isUrdu,
            utteranceId = "weekly_report_tts"
        )
    }

    // Weekly Report State
    var weeklyReport by remember { mutableStateOf<WeeklySatelliteReport?>(null) }
    var isGeneratingReport by remember { mutableStateOf(false) }
    var reportError by remember { mutableStateOf<String?>(null) }
    var isFullReportExpanded by remember { mutableStateOf(false) }

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Coordinates (NASA Grid):", color = TextSlateSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    if (AppLocationManager.hasLocationPermission(context)) {
                                        coroutineScope.launch {
                                            val res = AppLocationManager.fetchDeviceLocation(context)
                                            if (res.isSuccess) {
                                                val loc = res.getOrThrow()
                                                newFarmLat = String.format(Locale.US, "%.4f", loc.latitude)
                                                newFarmLon = String.format(Locale.US, "%.4f", loc.longitude)
                                            }
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.height(36.dp).testTag("btn_autofill_gps_farm")
                            ) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = AgriPrimaryFresh)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Autofill GPS", fontSize = 11.5.sp, color = AgriPrimaryFresh, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "7-Day Summary · ${report.farmName}",
                                    color = TextSlatePrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Crop: ${selectedFarm.crop.replaceFirstChar { it.uppercase() }}",
                                    color = TextSlateMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Voice Readout (TTS) for Weekly Report & Soil Moisture
                                OutlinedButton(
                                    onClick = { speakReport(report) },
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("button_listen_report_tts"),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSpeaking) AgriPrimaryContainer else Color.Transparent,
                                        contentColor = AgriPrimaryFresh
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSpeaking) AgriPrimaryFresh else SunlitBorder),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = if (isUrdu) "رپورٹ سنیں" else "Listen to Report",
                                        tint = if (isSpeaking) AgriPrimaryFresh else TextSlateSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSpeaking) (if (isUrdu) "روکیں" else "Stop") else (if (isUrdu) "سنیں" else "Listen"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSpeaking) AgriPrimaryFresh else TextSlateSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        val file = ReportPdfExporter.generateAndSharePdf(context, report, selectedFarm)
                                        if (file != null) {
                                            notificationAlertMessage = "📄 PDF Report generated: ${file.name}. Share dialog opened."
                                        }
                                    },
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("button_export_report_pdf"),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriPrimary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimaryFresh),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "Export PDF",
                                        tint = AlertRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Export PDF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgriPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                NasaLiveBadge()
                            }
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

                        // NASA GLDAS / POWER Root Zone Soil Moisture & Net Irrigation Requirement Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_soil_moisture_gldas"),
                            colors = CardDefaults.cardColors(containerColor = SunlitSurfaceSubtle),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = "Soil Moisture",
                                            tint = SkyBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Root Zone Soil Moisture (NASA GLDAS)",
                                            color = TextSlatePrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (report.soilMoisturePercent < 22.0) AlertRedBg
                                                else if (report.soilMoisturePercent <= 42.0) AgriPrimaryContainer
                                                else SkyBlueContainer
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = report.soilMoistureStatus,
                                            color = if (report.soilMoisturePercent < 22.0) AlertRed
                                            else if (report.soilMoisturePercent <= 42.0) AgriPrimary
                                            else SkyBlueText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${String.format(Locale.UK, "%.1f", report.soilMoisturePercent)}%",
                                            color = TextSlatePrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "0–100 cm profile saturation",
                                            color = TextSlateMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${String.format(Locale.UK, "%.0f", report.irrigationRequirementMm)} mm",
                                            color = if (report.irrigationRequirementMm > 0) SkyBlueText else AgriPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Net weekly crop water demand",
                                            color = TextSlateMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress bar visualizing moisture saturation
                                LinearProgressIndicator(
                                    progress = { (report.soilMoisturePercent / 100f).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (report.soilMoisturePercent < 22.0) SunGold else AgriPrimaryFresh,
                                    trackColor = SunlitBorder
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Year-over-Year Historical Climate Overlay & Trend Analysis Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_historical_climate_overlay"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SunlitBorder),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CompareArrows,
                                            contentDescription = "Historical Comparison",
                                            tint = AgriPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Historical Climate Trend Overlay",
                                            color = TextSlatePrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AgriPrimaryContainer.copy(alpha = 0.5f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "vs ${report.historicalYearLabel} (Same 7d)",
                                            color = AgriPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Comparative Metric Grid (Live 2026 vs Last Year 2025)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rainfall comparison box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SunlitSurfaceSubtle)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Rainfall Comparison",
                                                color = TextSlateMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "${String.format(Locale.UK, "%.1f", report.totalRainfallMm)}",
                                                    color = SkyBlueText,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = " vs ${String.format(Locale.UK, "%.1f", report.historicalTotalRainfallMm)} mm",
                                                    color = TextSlateSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (report.yoyRainfallDeltaMm >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                    contentDescription = "Rain Delta",
                                                    tint = if (report.yoyRainfallDeltaMm >= 0) SkyBlueText else AlertRed,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${String.format(Locale.UK, "%+.1f", report.yoyRainfallDeltaMm)} mm YoY",
                                                    color = if (report.yoyRainfallDeltaMm >= 0) SkyBlueText else AlertRed,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Temperature comparison box
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SunlitSurfaceSubtle)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Avg Temp Comparison",
                                                color = TextSlateMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = "${String.format(Locale.UK, "%.1f", report.avgTempC)}°",
                                                    color = SunGoldText,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = " vs ${String.format(Locale.UK, "%.1f", report.historicalAvgTempC)}°C",
                                                    color = TextSlateSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (report.yoyTempDeltaC >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                    contentDescription = "Temp Delta",
                                                    tint = if (report.yoyTempDeltaC > 1.5) AlertRed else SunGoldText,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${String.format(Locale.UK, "%+.1f", report.yoyTempDeltaC)}°C YoY",
                                                    color = if (report.yoyTempDeltaC > 1.5) AlertRed else SunGoldText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Agronomic Trend Analysis Sentence
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SunlitSurfaceSubtle)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Trend Analysis",
                                        tint = AgriPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = report.historicalTrendAnalysis,
                                        color = TextSlateSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
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
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Day / Date", color = TextSlatePrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                                    Text("Rain (Live vs '25)", color = SkyBlueText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                                    Text("Temp (Live vs '25)", color = SunGoldText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                                    Text("Wind (2m)", color = TextSlateSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                                }

                                // Table Rows with Historical Overlay
                                report.dailyRecords.forEachIndexed { idx, day ->
                                    val histDay = report.historicalDailyRecords.getOrNull(idx)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (idx % 2 == 0) Color.White else SunlitSurfaceSubtle)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(day.dateString, color = TextSlatePrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.1f))
                                        
                                        // Rainfall: live vs last year
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text(
                                                text = "${String.format(Locale.UK, "%.1f", day.rainfallMm)} mm",
                                                color = SkyBlueText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (histDay != null) {
                                                Text(
                                                    text = "Hist: ${String.format(Locale.UK, "%.1f", histDay.rainfallMm)} mm",
                                                    color = TextSlateMuted,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        // Temperature: live vs last year
                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text(
                                                text = "${String.format(Locale.UK, "%.1f", day.maxTempC ?: 0.0)}°C",
                                                color = SunGoldText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (histDay != null) {
                                                Text(
                                                    text = "Hist: ${String.format(Locale.UK, "%.1f", histDay.maxTempC ?: 0.0)}°C",
                                                    color = TextSlateMuted,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }

                                        // Wind
                                        Text(
                                            text = "${String.format(Locale.UK, "%.1f", day.windSpeedMs ?: 0.0)} m/s",
                                            color = TextSlateSecondary,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.weight(0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (report != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { generateReportForFarm(selectedFarm) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp)
                            .testTag("button_generate_weekly_report"),
                        enabled = !isGeneratingReport,
                        colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryFresh),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGeneratingReport) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Querying...", fontSize = 12.sp, color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Generate", tint = Color.White, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh Report", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val file = ReportPdfExporter.generateAndSharePdf(context, report, selectedFarm)
                            if (file != null) {
                                notificationAlertMessage = "📄 PDF Report generated: ${file.name}. Share dialog opened."
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("button_export_pdf_large"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimaryFresh),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgriPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = AgriPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = AgriPrimary)
                    }
                }
            } else {
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
            
            var tileLayer = L.tileLayer(gibsUrl, {
              maxZoom: 9,
              attribution: 'NASA Earthdata / GIBS &copy; OpenStreetMap'
            });
            tileLayer.on('tileerror', function(error) {
              console.warn('GIBS Tile request error:', (error && error.url) ? error.url : 'tile network error');
            });
            tileLayer.addTo(map);

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
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d(
                            "GIBS_Map",
                            "JS Console [${consoleMessage?.messageLevel()}]: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})"
                        )
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        android.util.Log.e("GIBS_Map", "Map load error [$errorCode]: $description at $failingUrl")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        error: android.webkit.WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        android.util.Log.e("GIBS_Map", "Map resource error [${error?.errorCode}]: ${error?.description} at ${request?.url}")
                    }

                    override fun onReceivedHttpError(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?,
                        errorResponse: android.webkit.WebResourceResponse?
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        android.util.Log.w("GIBS_Map", "HTTP error ${errorResponse?.statusCode} at ${request?.url}")
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: android.webkit.SslErrorHandler?,
                        error: android.net.http.SslError?
                    ) {
                        android.util.Log.e("GIBS_Map", "SSL Error: ${error?.primaryError} at ${error?.url}")
                        handler?.cancel()
                    }
                }
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
