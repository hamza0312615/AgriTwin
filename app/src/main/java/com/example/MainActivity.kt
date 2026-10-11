package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NasaNetworkState
import com.example.ui.components.OfflineStatusChip
import com.example.ui.screens.AlertsSoilScreen
import com.example.ui.screens.ClimateTwinScreen
import com.example.ui.screens.DiseaseAnalyzerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyFarmScreen
import com.example.ui.screens.WaterDashboardScreen
import com.example.ui.theme.AgriPrimaryContainer
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.AgriPrimaryText
import com.example.ui.theme.AgriTwinTheme
import com.example.ui.theme.SkyBlueContainer
import com.example.ui.theme.SkyBlueText
import com.example.ui.theme.SunlitBackground
import com.example.ui.theme.SunlitBorder
import com.example.ui.theme.SunlitSurface
import com.example.ui.theme.TextSlateMuted
import com.example.ui.theme.TextSlatePrimary

enum class AgriScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_item_home"),
    TWIN("Twin", Icons.Filled.Public, Icons.Outlined.Public, "nav_item_twin"),
    WATER("Water", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop, "nav_item_water"),
    FARM("My Farm", Icons.Filled.Terrain, Icons.Outlined.Terrain, "nav_item_farm"),
    DISEASE("Disease", Icons.Filled.Biotech, Icons.Outlined.Biotech, "nav_item_disease"),
    ALERTS("Alerts", Icons.Filled.Warning, Icons.Outlined.Warning, "nav_item_alerts")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Monitor network connectivity and update NasaNetworkState
        setupNetworkMonitoring()

        // Initialize local notification channel for crop disease risk alerts
        com.example.util.DiseaseAlertNotificationManager.createNotificationChannel(this)

        val targetScreen = if (intent?.getStringExtra("EXTRA_NAV_TARGET") == "FARM") {
            AgriScreen.FARM
        } else {
            AgriScreen.HOME
        }

        setContent {
            AgriTwinTheme {
                AgriTwinApp(initialScreen = targetScreen)
            }
        }
    }

    private fun setupNetworkMonitoring() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!hasInternet) {
            NasaNetworkState.isOffline = true
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        NasaNetworkState.isOffline = false
                    }
                }

                override fun onLost(network: Network) {
                    runOnUiThread {
                        NasaNetworkState.isOffline = true
                    }
                }
            }
        )
    }
}

@Composable
fun AgriTwinApp(initialScreen: AgriScreen = AgriScreen.HOME) {
    var currentScreen by remember { mutableStateOf(initialScreen) }

    // Predictive system back: return to HOME from any secondary tab
    if (currentScreen != AgriScreen.HOME) {
        BackHandler {
            currentScreen = AgriScreen.HOME
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SunlitBackground),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = SunlitSurface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = SunlitBorder)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .testTag("app_top_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AgriPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "AgriTwin Logo",
                                tint = AgriPrimaryFresh,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AgriTwin",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = TextSlatePrimary
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "NASA 2026",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = SkyBlueText,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SkyBlueContainer)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "Climate-Twin Farming Intelligence",
                                fontSize = 10.sp,
                                color = TextSlateMuted,
                                maxLines = 1
                            )
                        }
                    }

                    // Subtle Offline Status Indicator (appears when offline or NASA API calls fail)
                    OfflineStatusChip(isOffline = NasaNetworkState.isOffline)
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .border(width = 1.dp, color = SunlitBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = SunlitSurface,
                tonalElevation = 3.dp
            ) {
                AgriScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(21.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgriPrimaryText,
                            selectedTextColor = AgriPrimaryFresh,
                            indicatorColor = AgriPrimaryContainer,
                            unselectedIconColor = TextSlateMuted,
                            unselectedTextColor = TextSlateMuted
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AgriScreen.HOME -> HomeScreen(
                    onNavigateToFarm = { currentScreen = AgriScreen.FARM },
                    onNavigateToTwin = { currentScreen = AgriScreen.TWIN },
                    onNavigateToWater = { currentScreen = AgriScreen.WATER },
                    onNavigateToDisease = { currentScreen = AgriScreen.DISEASE },
                    onNavigateToAlerts = { currentScreen = AgriScreen.ALERTS }
                )
                AgriScreen.TWIN -> ClimateTwinScreen(
                    onNavigateBack = { currentScreen = AgriScreen.HOME }
                )
                AgriScreen.WATER -> WaterDashboardScreen()
                AgriScreen.FARM -> MyFarmScreen()
                AgriScreen.DISEASE -> DiseaseAnalyzerScreen(
                    onNavigateBack = { currentScreen = AgriScreen.HOME }
                )
                AgriScreen.ALERTS -> AlertsSoilScreen(
                    onNavigateBack = { currentScreen = AgriScreen.HOME }
                )
            }
        }
    }
}
