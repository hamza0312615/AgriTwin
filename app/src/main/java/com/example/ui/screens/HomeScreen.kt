package com.example.ui.screens

import android.speech.tts.TextToSpeech
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.data.location.AppLocationManager
import com.example.data.model.ClimatologyData
import com.example.data.network.NasaPowerRepository
import com.example.ui.components.LocationSelectorCard
import com.example.util.AppLanguageState
import com.example.util.AppTtsManager
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
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var liveTelemetry by remember { mutableStateOf<ClimatologyData?>(null) }
    var refreshTimestamp by remember { mutableStateOf<String?>("Live") }
    val isUrdu = AppLanguageState.isUrdu
    var showDeveloperOptions by remember { mutableStateOf(false) }
    val isSpeaking = AppTtsManager.isSpeaking

    fun speakBriefing(textEn: String, textUr: String) {
        AppTtsManager.speak(
            context = context,
            textEn = textEn,
            textUr = textUr,
            isUrdu = isUrdu,
            utteranceId = "daily_briefing_speech"
        )
    }

    val currentLocation by AppLocationManager.currentLocation.collectAsState()

    fun refreshData(
        lat: Double = currentLocation.latitude,
        lon: Double = currentLocation.longitude
    ) {
        coroutineScope.launch {
            isRefreshing = true
            val res = repository.fetchClimatology(lat, lon)
            if (res.isSuccess) {
                liveTelemetry = res.getOrThrow()
                refreshTimestamp = "Live · Updated"
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(currentLocation.latitude, currentLocation.longitude) {
        refreshData(currentLocation.latitude, currentLocation.longitude)
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
            // Top App Bar with Title & EN / اردو Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AgriPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "AgriTwin Logo",
                            tint = AgriPrimaryFresh,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isUrdu) "ایگری ٹوئن" else "AgriTwin",
                            color = TextSlatePrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = if (isUrdu) "زرعی سیٹلائٹ انٹیلیجنس" else "Climate-Twin Farming Intelligence",
                            color = SkyBlue,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // EN / اردو Toggle Switch Button (min 48px touch target)
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.5.dp, if (isUrdu) AgriPrimaryFresh else SunlitBorder, RoundedCornerShape(24.dp))
                        .clickable { AppLanguageState.toggleLanguage() }
                        .padding(horizontal = 14.dp)
                        .testTag("btn_language_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = if (isUrdu) AgriPrimaryFresh else TextSlateSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = if (isUrdu) "اردو (UR)" else "EN / اردو",
                            color = if (isUrdu) AgriPrimaryFresh else TextSlatePrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Developer / Debug Visibility Toggle Ribbon (Subtle, hidden by default)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(AgriPrimaryFresh))
                    Spacer(modifier = Modifier.width(6.dp))
                    val telem = liveTelemetry
                    val locLabel = currentLocation.displayName.take(24)
                    if (telem != null) {
                        Text(
                            text = if (isUrdu) "$locLabel: ${String.format(Locale.UK, "%.1f°C", telem.meanAnnualTempC)} · لائیو ناسا" else "$locLabel: ${String.format(Locale.UK, "%.1f°C", telem.meanAnnualTempC)} · Live NASA POWER",
                            color = TextSlateSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = if (isUrdu) "ناسا زمینی مشاہدات" else "NASA Earth Observation Telemetry",
                            color = TextSlateSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Subtle Developer Options Toggle (min 48px touch target)
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDeveloperOptions = !showDeveloperOptions }
                        .padding(horizontal = 8.dp)
                        .testTag("toggle_developer_options"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Developer Options",
                        tint = if (showDeveloperOptions) SkyBlue else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showDeveloperOptions) (if (isUrdu) "ڈویلپر آپشنز آن" else "Dev Mode On") else (if (isUrdu) "ڈویلپر آپشنز" else "Developer"),
                        color = if (showDeveloperOptions) SkyBlue else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Visible NASA Request URL Debug Ribbon (Hidden behind Developer toggle per Part 2)
            if (showDeveloperOptions) {
                liveTelemetry?.requestUrl?.let { url ->
                    if (url.isNotBlank()) {
                        RequestUrlDebugBanner(url = url, modifier = Modifier.padding(bottom = 10.dp))
                    }
                }
            }

            // Hero Card with Canvas Vector Illustration (Satellite over fields)
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
                            .height(140.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Selector & FusedLocationProviderClient GPS Controller
            LocationSelectorCard(
                onLocationChanged = { lat, lon ->
                    refreshData(lat, lon)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PROMINENT "Today on Your Farm" DAILY BRIEFING CARD
            // Replaces the large descriptive paragraph with actionable farming glance
            val currentTempC = liveTelemetry?.meanAnnualTempC ?: 27.4
            val currentPrecipMm = (liveTelemetry?.meanAnnualPrecipitationMm?.div(12.0) ?: 31.0)
            val briefingAdviceEn = "Canopy moisture is stable. Ideal window for evening canal irrigation; avoid chemical spraying during midday heat."
            val briefingAdviceUr = "فصل میں نمی کی مقدار تسلی بخش ہے۔ شام کے وقت آبپاشی کے لیے بہترین وقت ہے؛ دوپہر کی سخت دھوپ میں سپرے سے پرہیز کریں۔"
            val locName = currentLocation.displayName
            val speechTextEn = "Daily Agronomic Briefing for $locName. Temperature is ${currentTempC.toInt()} degrees Celsius. Rainfall is ${currentPrecipMm.toInt()} millimetres. Advice: $briefingAdviceEn"
            val speechTextUr = "روزانہ زرعی بریفنگ برائے $locName۔ درجہ حرارت ${currentTempC.toInt()} ڈگری سینٹی گریڈ ہے۔ بارش ${currentPrecipMm.toInt()} ملی میٹر ہے۔ مشورہ: $briefingAdviceUr"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF86EFAC), RoundedCornerShape(20.dp))
                    .testTag("card_daily_briefing"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Briefing Header with Speaker Icon (min 48px touch target)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Briefing",
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isUrdu) "آج آپ کے فارم پر" else "Today on Your Farm",
                                    color = TextSlatePrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isUrdu) "روزانہ زرعی رہنمائی اور موسمی کیفیت" else "Daily Satellite Agronomic Briefing",
                                    color = TextSlateMuted,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        // Speaker Voice Readout Button (≥ 48px touch target)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) Color(0xFFFEF08A) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSpeaking) Color(0xFFEAB308) else Color(0xFFCBD5E1), CircleShape)
                                .clickable { speakBriefing(speechTextEn, speechTextUr) }
                                .testTag("btn_speaker_daily_briefing"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isSpeaking) (if (isUrdu) "آواز بند کریں" else "Stop Reading") else (if (isUrdu) "روزانہ بریفنگ سنیں" else "Listen to Daily Briefing"),
                                tint = if (isSpeaking) Color(0xFF854D0E) else TextSlatePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weather Metric Chips: Temperature & Precipitation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Temperature Metric
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Thermostat,
                                    contentDescription = "Temperature",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isUrdu) "درجہ حرارت" else "Temperature",
                                        color = TextSlateMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${String.format(Locale.UK, "%.1f", currentTempC)}°C",
                                        color = Color(0xFF991B1B),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Precipitation Metric
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Rainfall",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isUrdu) "بارش" else "Rainfall",
                                        color = TextSlateMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${currentPrecipMm.toInt()} mm",
                                        color = Color(0xFF1E40AF),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // One Clear, Actionable Farming Tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0FDF4))
                            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Grass,
                                contentDescription = "Farming Advice",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isUrdu) "آج کا عملی مشورہ:" else "Today's Actionable Advice:",
                                    color = Color(0xFF14532D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isUrdu) briefingAdviceUr else briefingAdviceEn,
                                    color = Color(0xFF166534),
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

        // Visible Honesty Strip
        HonestyStrip()

        Spacer(modifier = Modifier.height(20.dp))

        // ACTION CARDS: 3 large, tappable visual cards (Twin Climate, Water Outlook, Satellite Health) with >=48px touch targets
        Text(
            text = if (isUrdu) "فوری اقدامات" else "Action Cards",
            color = TextSlatePrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (isUrdu) "سیٹلائٹ ٹیلی میٹری اور فیلڈ ڈیٹا تک براہ راست رسائی" else "Direct satellite telemetry & climate-resilient field tools",
            color = TextSlateMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Card 1: Twin Climate (Connect fields via satellite)
        ActionLargeCard(
            title = if (isUrdu) "ٹوئن کلائمیٹ (موسمی ہمزاد)" else "Twin Climate",
            tag = if (isUrdu) "سیٹلائٹ میچنگ" else "Global Climate Twins",
            subtitle = if (isUrdu) "اپنے رقبے کا عالمی موسمی ہمزاد تلاش کریں اور آزمودہ فصلیں دریافت کریں۔" else "Find global regions matching your farm's projected climate to deploy proven crop rotations.",
            icon = Icons.Default.Public,
            iconTint = AgriPrimaryFresh,
            containerBg = Color(0xFFF0FDF4),
            borderTint = Color(0xFFBBF7D0),
            onClick = onNavigateToTwin,
            testTag = "card_action_twin_climate"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Card 2: Water Outlook (14-day rainfall + AQUORA)
        ActionLargeCard(
            title = if (isUrdu) "پانی کا تجزیہ (واٹر آؤٹ لک)" else "Water Outlook",
            tag = if (isUrdu) "لائیو ناسا بارش" else "Live Precipitation & Moisture",
            subtitle = if (isUrdu) "چودہ روزہ بارش کا رجحان اور کیمیکلز سے پاک زمینی نمی کا حساب۔" else "Track 14-day precipitation time-series and rootzone saturation balance to optimise irrigation.",
            icon = Icons.Default.WaterDrop,
            iconTint = SkyBlue,
            containerBg = Color(0xFFEFF6FF),
            borderTint = Color(0xFFBFDBFE),
            onClick = onNavigateToWater,
            testTag = "card_action_water_outlook"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Card 3: Satellite Health (NASA GIBS True-Colour + Disease risk)
        ActionLargeCard(
            title = if (isUrdu) "سیٹلائٹ صحت اور فارم مانیٹر" else "Satellite Health",
            tag = if (isUrdu) "لائیو ناسا گبز" else "NASA GIBS Observation",
            subtitle = if (isUrdu) "اصلی سیٹلائٹ تصاویر سے اپنے رقبے کی فصل کی صحت اور ہفتہ وار رپورٹ دیکھیں۔" else "Inspect high-resolution true-colour satellite imagery of your fields and generate weekly health reports.",
            icon = Icons.Default.Terrain,
            iconTint = Color(0xFFD97706),
            containerBg = Color(0xFFFFFBEB),
            borderTint = Color(0xFFFDE68A),
            onClick = onNavigateToFarm,
            testTag = "card_action_satellite_health"
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

        // Professional Footer with TEAM DEVTECH and NASA Space Apps 2026 tags
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // Team DevTech & NASA Space Apps Tags moved to footer per user request
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AgriPrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AgriPrimaryFresh)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TEAM DEVTECH",
                            color = AgriPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    NasaLiveBadge(text = "NASA Space Apps 2026")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isUrdu) "ایگری ٹوئن · تیار کردہ ٹیم ڈیوٹیک" else "AgriTwin · Climate-Twin Farming Intelligence",
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
 * Large, tappable Action Card for the 3 primary farmer CUJs:
 * Twin Climate, Water Outlook, Satellite Health.
 * High contrast, minimum 48px touch targets, clear badge and arrow navigation.
 */
@Composable
private fun ActionLargeCard(
    title: String,
    tag: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerBg: Color,
    borderTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isPressed) 0.985f else 1f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .border(1.5.dp, borderTint, RoundedCornerShape(18.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp) // Minimum 48px touch/visual target
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, borderTint, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = TextSlatePrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                color = iconTint,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Arrow Circle Button (48px touch target)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, borderTint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open $title",
                        tint = TextSlatePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                color = TextSlateSecondary,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
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

        // 4. Satellite at orbit apex (~ (w * 0.38f, h * 0.23f))
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
