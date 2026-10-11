package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.AppLocationManager
import com.example.data.location.UserLocation
import com.example.data.model.PRESET_DISTRICTS
import com.example.ui.theme.AgriPrimary
import com.example.ui.theme.AgriPrimaryBright
import com.example.ui.theme.AgriPrimaryContainer
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.AgriPrimaryText
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.AlertRedBorder
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueText
import com.example.ui.theme.SunGold
import com.example.ui.theme.SunGoldContainer
import com.example.ui.theme.SunGoldText
import com.example.ui.theme.SunlitBackground
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.SunlitSurfaceCard
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlatePrimary
import com.example.ui.theme.TextSlateSecondary
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Interactive Location Selector Card with FusedLocationProviderClient integration,
 * dynamic GPS fix detection, and fallback to manual coordinate input / agricultural presets.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationSelectorCard(
    modifier: Modifier = Modifier,
    onLocationChanged: (latitude: Double, longitude: Double) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentLocation by AppLocationManager.currentLocation.collectAsState()
    val isLoadingLocation by AppLocationManager.isLoadingLocation.collectAsState()
    val locationError by AppLocationManager.locationError.collectAsState()
    val isPermissionDenied by AppLocationManager.isPermissionDenied.collectAsState()

    var showManualDialog by remember { mutableStateOf(false) }

    // Runtime permission launcher for FusedLocationProviderClient
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                val result = AppLocationManager.fetchDeviceLocation(context)
                if (result.isSuccess) {
                    val loc = result.getOrThrow()
                    onLocationChanged(loc.latitude, loc.longitude)
                } else {
                    // Fallback to manual dialog if GPS fix failed
                    showManualDialog = true
                }
            }
        } else {
            // Permission denied -> activate fallback to manual input
            AppLocationManager.onPermissionDenied()
            showManualDialog = true
        }
    }

    fun requestGpsLocation() {
        if (AppLocationManager.hasLocationPermission(context)) {
            coroutineScope.launch {
                val result = AppLocationManager.fetchDeviceLocation(context)
                if (result.isSuccess) {
                    val loc = result.getOrThrow()
                    onLocationChanged(loc.latitude, loc.longitude)
                } else {
                    showManualDialog = true
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
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SunlitBorder, RoundedCornerShape(14.dp))
            .testTag("location_selector_card"),
        color = SunlitSurfaceCard,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Active Farm Coordinate & GPS Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentLocation.isGpsFixed) AgriPrimaryContainer else SkyBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentLocation.isGpsFixed) Icons.Default.GpsFixed else Icons.Default.Place,
                            contentDescription = "Location Status",
                            tint = if (currentLocation.isGpsFixed) AgriPrimaryFresh else SkyBlueText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ACTIVE LOCATION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSlateMuted,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (currentLocation.isGpsFixed) AgriPrimaryContainer else SunGoldContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentLocation.isGpsFixed) "GPS LIVE" else "MANUAL / PRESET",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (currentLocation.isGpsFixed) AgriPrimaryText else SunGoldText
                                )
                            }
                        }
                        Text(
                            text = currentLocation.displayName,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSlatePrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SunlitBackground)
                    .border(1.dp, SunlitBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Coordinates",
                        tint = AgriPrimaryFresh,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NASA Grid: ${currentLocation.formattedCoordinates}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSlateSecondary
                    )
                }

                if (currentLocation.accuracyMeters != null) {
                    Text(
                        text = "±${currentLocation.accuracyMeters!!.toInt()}m",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSlateMuted
                    )
                }
            }

            // Warning Banner if permission was denied or error occurred
            AnimatedVisibility(visible = isPermissionDenied || locationError != null) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AlertRedBg)
                            .border(1.dp, AlertRedBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Permission Denied",
                                tint = AlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = locationError ?: "GPS access denied. Use manual coordinates below.",
                                fontSize = 11.5.sp,
                                color = AlertRed,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { AppLocationManager.dismissPermissionNotice() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = AlertRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Action Buttons (Min 48dp Touch Targets):
            // 1) "Use GPS Location" (FusedLocationProviderClient)
            // 2) "Manual / Preset" (Fallback Input)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Detect GPS
                Button(
                    onClick = { requestGpsLocation() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_detect_gps"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriPrimaryFresh,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detecting...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLocation.isGpsFixed) "Re-scan GPS" else "Use Device GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Button 2: Manual Input / Presets (Fallback)
                OutlinedButton(
                    onClick = { showManualDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_edit_coords_manual"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSlatePrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditLocation,
                        contentDescription = "Manual Input",
                        tint = AgriPrimaryFresh,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manual Coords",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Fallback Manual Coordinate Input Dialog
    if (showManualDialog) {
        ManualCoordinateInputDialog(
            initialLat = currentLocation.latitude,
            initialLon = currentLocation.longitude,
            isPermissionDenied = isPermissionDenied,
            onDismiss = {
                showManualDialog = false
                AppLocationManager.dismissPermissionNotice()
            },
            onSave = { lat, lon, name ->
                showManualDialog = false
                val success = AppLocationManager.setManualCoordinates(lat, lon, name)
                if (success) {
                    onLocationChanged(lat, lon)
                }
            }
        )
    }
}

/**
 * Dialog enabling farmers to enter custom coordinates or choose from NASA agricultural presets
 * whenever GPS permission is denied or coordinates need custom fine-tuning.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualCoordinateInputDialog(
    initialLat: Double,
    initialLon: Double,
    isPermissionDenied: Boolean,
    onDismiss: () -> Unit,
    onSave: (latitude: Double, longitude: Double, name: String) -> Unit
) {
    var latText by remember { mutableStateOf(String.format(Locale.US, "%.4f", initialLat)) }
    var lonText by remember { mutableStateOf(String.format(Locale.US, "%.4f", initialLon)) }
    var locationLabel by remember { mutableStateOf("Custom Field") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Manual Location",
                    tint = AgriPrimaryFresh,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Farm Location Coordinates",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlatePrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isPermissionDenied) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AlertRedBg)
                            .border(1.dp, AlertRedBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Denied Notice",
                                tint = AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GPS permission was denied. Enter manual latitude & longitude coordinates or select a preset agricultural district below.",
                                fontSize = 11.5.sp,
                                color = AlertRed,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = "Enter precise coordinates for your agricultural fields or choose a NASA regional baseline:",
                        fontSize = 12.sp,
                        color = TextSlateSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Latitude Field
                OutlinedTextField(
                    value = latText,
                    onValueChange = {
                        latText = it
                        errorMessage = null
                    },
                    label = { Text("Latitude (-90.0 to 90.0°)") },
                    placeholder = { Text("e.g. 31.4187") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_latitude"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgriPrimaryFresh,
                        cursorColor = AgriPrimaryFresh
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Longitude Field
                OutlinedTextField(
                    value = lonText,
                    onValueChange = {
                        lonText = it
                        errorMessage = null
                    },
                    label = { Text("Longitude (-180.0 to 180.0°)") },
                    placeholder = { Text("e.g. 73.0791") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_longitude"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgriPrimaryFresh,
                        cursorColor = AgriPrimaryFresh
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location Label Field
                OutlinedTextField(
                    value = locationLabel,
                    onValueChange = { locationLabel = it },
                    label = { Text("Farm / Field Label") },
                    placeholder = { Text("e.g. Chenab Canal Basin") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_label"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgriPrimaryFresh,
                        cursorColor = AgriPrimaryFresh
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = AlertRed,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Presets (NASA Reference Hubs):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSlateMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Quick Presets FlowRow
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PRESET_DISTRICTS.forEach { district ->
                        val isSelected = latText.toDoubleOrNull()?.let { lat ->
                            Math.abs(lat - district.latitude) < 0.001
                        } == true

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                latText = String.format(Locale.US, "%.4f", district.latitude)
                                lonText = String.format(Locale.US, "%.4f", district.longitude)
                                locationLabel = "${district.name}, ${district.country}"
                                errorMessage = null
                            },
                            label = { Text(district.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriPrimaryContainer,
                                selectedLabelColor = AgriPrimaryText
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lon = lonText.toDoubleOrNull()
                    if (lat == null || lat !in -90.0..90.0) {
                        errorMessage = "Invalid Latitude. Enter a value between -90.0 and 90.0"
                        return@Button
                    }
                    if (lon == null || lon !in -180.0..180.0) {
                        errorMessage = "Invalid Longitude. Enter a value between -180.0 and 180.0"
                        return@Button
                    }
                    onSave(lat, lon, locationLabel.ifBlank { "Custom Field" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgriPrimaryFresh),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_apply_manual_location")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Apply & Fetch NASA Data", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Cancel", color = TextSlateMuted)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}
