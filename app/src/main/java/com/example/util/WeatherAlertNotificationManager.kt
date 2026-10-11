package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.DailyRainfallRecord
import com.example.data.model.SavedFarm
import com.example.data.model.WeatherAlert
import java.util.Locale

/**
 * Weather Alert Type for Critical Weather Thresholds.
 */
enum class WeatherAlertType(val title: String) {
    HEAVY_RAINFALL("Heavy Rainfall Alert"),
    EXTREME_HEAT("Extreme Heatwave Alert"),
    GALE_WIND("High Velocity Wind Alert"),
    FLOOD_INUNDATION("Flash Flood Alert")
}

data class WeatherAlertTriggerResult(
    val triggered: Boolean,
    val alertType: WeatherAlertType?,
    val title: String,
    val message: String,
    val notificationSent: Boolean
)

/**
 * Local Notification System for Critical Weather Telemetry Changes.
 * Evaluates live NASA POWER weather data for heavy rainfall, extreme heatwaves, and gale winds,
 * and posts high-priority system alerts to protect farmers' crops and infrastructure.
 */
object WeatherAlertNotificationManager {

    const val CHANNEL_ID = "agritwin_weather_hazard_alerts"
    const val CHANNEL_NAME = "Critical Weather Hazard Alerts"
    const val CHANNEL_DESCRIPTION = "Alerts farmers for critical weather changes such as heavy rainfall forecasts and extreme heatwaves"

    private const val PREFS_NAME = "agritwin_weather_alert_prefs"
    private const val KEY_LAST_ALERT_PREFIX = "last_alert_time_"

    // Thresholds per NASA Agro-Climatology specifications:
    const val HEAVY_RAIN_THRESHOLD_MM = 35.0 // mm in a single day or 50mm multi-day
    const val EXTREME_HEAT_THRESHOLD_C = 40.0 // Celsius max daily temperature
    const val GALE_WIND_THRESHOLD_MS = 10.0 // m/s (approx 36 km/h)

    /**
     * Initializes the notification channel on Android 8.0+ (API 26+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Check whether POST_NOTIFICATIONS permission is granted (Android 13+ / API 33+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Evaluates live daily records from NASA POWER and dispatches notifications if critical thresholds are met.
     */
    fun checkAndNotifyCriticalWeather(
        context: Context,
        farmName: String = "Your Farm",
        records: List<DailyRainfallRecord>,
        cropName: String = "Crops"
    ): WeatherAlertTriggerResult {
        createNotificationChannel(context)

        if (records.isEmpty()) {
            return WeatherAlertTriggerResult(
                triggered = false,
                alertType = null,
                title = "No Data",
                message = "No live telemetry available",
                notificationSent = false
            )
        }

        // Find worst weather events in recent window
        val peakRainRecord = records.maxByOrNull { it.rainfallMm }
        val peakTempRecord = records.filter { it.maxTempC != null }.maxByOrNull { it.maxTempC ?: 0.0 }
        val peakWindRecord = records.filter { it.windSpeedMs != null }.maxByOrNull { it.windSpeedMs ?: 0.0 }

        // Check 1: Extreme Heat (≥ 40°C or 42°C)
        val peakTemp = peakTempRecord?.maxTempC ?: 0.0
        if (peakTemp >= EXTREME_HEAT_THRESHOLD_C) {
            val title = "☀️ Extreme Heat Alert: ${peakTemp.toInt()}°C · $farmName"
            val body = "Dangerous thermal spike of ${String.format(Locale.UK, "%.1f", peakTemp)}°C detected on ${peakTempRecord?.dateString}. Risk of canopy desiccation & floral sterility in $cropName."
            val action = "Apply night-time micro-irrigation to cool rootzone; avoid daytime fertilisation."
            val sent = postWeatherNotification(context, 2001, title, body, action, "ALERTS")
            return WeatherAlertTriggerResult(
                triggered = true,
                alertType = WeatherAlertType.EXTREME_HEAT,
                title = title,
                message = body,
                notificationSent = sent
            )
        }

        // Check 2: Heavy Rainfall (≥ 35 mm/day)
        val peakRain = peakRainRecord?.rainfallMm ?: 0.0
        if (peakRain >= HEAVY_RAIN_THRESHOLD_MM) {
            val title = "🌧️ Heavy Rainfall Alert: ${peakRain.toInt()} mm · $farmName"
            val body = "Torrential precipitation of ${String.format(Locale.UK, "%.1f", peakRain)} mm observed on ${peakRainRecord?.dateString}. Severe soil waterlogging & root asphyxiation risk."
            val action = "Open drainage bunds immediately; postpone foliar spraying until soils drain."
            val sent = postWeatherNotification(context, 2002, title, body, action, "ALERTS")
            return WeatherAlertTriggerResult(
                triggered = true,
                alertType = WeatherAlertType.HEAVY_RAINFALL,
                title = title,
                message = body,
                notificationSent = sent
            )
        }

        // Check 3: Gale Wind (≥ 10 m/s or 36 km/h)
        val peakWind = peakWindRecord?.windSpeedMs ?: 0.0
        if (peakWind >= GALE_WIND_THRESHOLD_MS) {
            val kmh = (peakWind * 3.6).toInt()
            val title = "💨 Gale Wind Alert: $kmh km/h · $farmName"
            val body = "High-velocity wind gusts of $kmh km/h recorded on ${peakWindRecord?.dateString}. Risk of tall crop lodging in $cropName."
            val action = "Anchor shade nets and delay aerial pesticide spraying until calm conditions return."
            val sent = postWeatherNotification(context, 2003, title, body, action, "ALERTS")
            return WeatherAlertTriggerResult(
                triggered = true,
                alertType = WeatherAlertType.GALE_WIND,
                title = title,
                message = body,
                notificationSent = sent
            )
        }

        return WeatherAlertTriggerResult(
            triggered = false,
            alertType = null,
            title = "Normal Conditions",
            message = "All parameters within standard thresholds",
            notificationSent = false
        )
    }

    /**
     * Send specific test or simulation notifications so the farmer/evaluator can immediately verify alerts.
     */
    fun sendSimulatedAlert(
        context: Context,
        type: WeatherAlertType,
        farmName: String = "Chak 217 Canal Colony",
        cropName: String = "Wheat"
    ): Boolean {
        createNotificationChannel(context)
        return when (type) {
            WeatherAlertType.EXTREME_HEAT -> {
                val title = "☀️ Extreme Heat Alert: 43°C · $farmName"
                val body = "CRITICAL THERMAL SPIKE: 43.2°C recorded via NASA POWER. High evapotranspiration rate endangering $cropName yields."
                val action = "Initiate emergency light night-pulse irrigation. Keep soil shaded."
                postWeatherNotification(context, 2001, title, body, action, "ALERTS")
            }
            WeatherAlertType.HEAVY_RAINFALL -> {
                val title = "🌧️ Heavy Rainfall Alert: 58 mm · $farmName"
                val body = "TORRENTIAL DOWNPOUR DETECTED: 58.4 mm precipitation recorded. Imminent waterlogging on clay-loam fields."
                val action = "Clear field drain exits, pump standing canal water, halt fertilizer spread."
                postWeatherNotification(context, 2002, title, body, action, "ALERTS")
            }
            WeatherAlertType.GALE_WIND -> {
                val title = "💨 Gale Wind Alert: 48 km/h · $farmName"
                val body = "HIGH GUSTS WARNING: 13.4 m/s (48 km/h) detected. Tall crop lodging risk for $cropName."
                val action = "Suspend high spray booms and secure field nursery structures."
                postWeatherNotification(context, 2003, title, body, action, "ALERTS")
            }
            WeatherAlertType.FLOOD_INUNDATION -> {
                val title = "🌊 Flash Flood Alert · $farmName"
                val body = "RIVER CATCHMENT SURGE: Upstream Indus/Chenab saturation. Floodplain runoff risk within 24-36h."
                val action = "Reinforce perimeter bunds and elevate mobile power generators."
                postWeatherNotification(context, 2004, title, body, action, "ALERTS")
            }
        }
    }

    private fun postWeatherNotification(
        context: Context,
        notificationId: Int,
        title: String,
        bodyText: String,
        actionAdvice: String,
        navTarget: String
    ): Boolean {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return false
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_TARGET", navTarget)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullText = "$bodyText\n\n🛰️ Telemetry: NASA POWER Earth Observation\n⚡ Urgent Action: $actionAdvice"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
