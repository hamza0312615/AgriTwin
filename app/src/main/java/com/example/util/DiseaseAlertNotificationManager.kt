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
import com.example.data.model.DiseaseRiskLevel
import com.example.data.model.SavedFarm
import com.example.data.model.WeeklySatelliteReport
import java.util.Locale

/**
 * Result data class for risk level transition check.
 */
data class RiskTransitionResult(
    val escalatedToHigh: Boolean,
    val previousLevel: DiseaseRiskLevel,
    val currentLevel: DiseaseRiskLevel,
    val notificationSent: Boolean,
    val message: String
)

/**
 * Local Notification System for AgriTwin.
 * Alerts user when 'Disease Risk' level in their weekly satellite report
 * moves from 'Low' (or moderate) to 'High' based on fetched NASA POWER weather telemetry.
 */
object DiseaseAlertNotificationManager {

    const val CHANNEL_ID = "agritwin_disease_alerts"
    const val CHANNEL_NAME = "Crop Disease Risk Alerts"
    const val CHANNEL_DESCRIPTION = "Alerts when satellite weather indicates crop disease risk changes to High"
    private const val PREFS_NAME = "agritwin_notification_prefs"
    private const val KEY_PREFIX_RISK = "risk_level_"

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
     * Retrieves stored previous risk level for a farm.
     */
    fun getPreviousRiskLevel(context: Context, farmId: String): DiseaseRiskLevel {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString("$KEY_PREFIX_RISK$farmId", DiseaseRiskLevel.LOW.name)
        return try {
            DiseaseRiskLevel.valueOf(savedName ?: DiseaseRiskLevel.LOW.name)
        } catch (e: Exception) {
            DiseaseRiskLevel.LOW
        }
    }

    /**
     * Updates stored risk level for a farm.
     */
    fun setPreviousRiskLevel(context: Context, farmId: String, level: DiseaseRiskLevel) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("$KEY_PREFIX_RISK$farmId", level.name).apply()
    }

    /**
     * Resets stored risk level to LOW for testing transitions.
     */
    fun resetFarmRiskToLow(context: Context, farmId: String) {
        setPreviousRiskLevel(context, farmId, DiseaseRiskLevel.LOW)
    }

    /**
     * Checks if Disease Risk has moved from 'Low' to 'High' and triggers local notification.
     */
    fun checkAndNotifyDiseaseRiskChange(
        context: Context,
        farm: SavedFarm,
        report: WeeklySatelliteReport
    ): RiskTransitionResult {
        createNotificationChannel(context)

        val previousLevel = getPreviousRiskLevel(context, farm.id)
        val currentLevel = report.diseaseRiskLevel

        // Save current level for next comparison
        setPreviousRiskLevel(context, farm.id, currentLevel)

        val isEscalation = (previousLevel == DiseaseRiskLevel.LOW || previousLevel == DiseaseRiskLevel.MODERATE) &&
                currentLevel == DiseaseRiskLevel.HIGH

        if (isEscalation) {
            val sent = sendHighDiseaseRiskNotification(
                context = context,
                farm = farm,
                rainfallMm = report.totalRainfallMm,
                avgTempC = report.avgTempC,
                customNotice = report.diseaseRiskNote
            )
            return RiskTransitionResult(
                escalatedToHigh = true,
                previousLevel = previousLevel,
                currentLevel = currentLevel,
                notificationSent = sent,
                message = "Disease risk escalated from ${previousLevel.label} to HIGH based on satellite weather telemetry. Local alert dispatched."
            )
        }

        return RiskTransitionResult(
            escalatedToHigh = false,
            previousLevel = previousLevel,
            currentLevel = currentLevel,
            notificationSent = false,
            message = "Disease risk steady at ${currentLevel.label}."
        )
    }

    /**
     * Dispatches the local high-importance notification.
     */
    fun sendHighDiseaseRiskNotification(
        context: Context,
        farm: SavedFarm,
        rainfallMm: Double,
        avgTempC: Double,
        customNotice: String? = null
    ): Boolean {
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return false
        }

        val notificationId = 1000 + (farm.id.hashCode() % 1000).let { if (it < 0) -it else it }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_TARGET", "FARM")
            putExtra("EXTRA_FARM_ID", farm.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ High Disease Risk Alert · ${farm.name}"
        val contentText = "Warmth (${String.format(Locale.UK, "%.1f", avgTempC)}°C) & rain (${String.format(Locale.UK, "%.1f", rainfallMm)} mm) escalated ${farm.crop} disease risk from LOW to HIGH."
        val bigText = (customNotice ?: contentText) +
                "\n\n🛰️ Telemetry: NASA POWER Earth Observation\n🌾 Crop: ${farm.crop} · Chak/Location: ${String.format(Locale.UK, "%.2f, %.2f", farm.latitude, farm.longitude)}\nAction: Inspect field borders & prepare preventive fungicide/drainage."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
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

    /**
     * Simulate Low to High transition for testing & interactive demo.
     */
    fun simulateRiskEscalation(context: Context, farm: SavedFarm): Boolean {
        // Reset to Low first to simulate the jump
        resetFarmRiskToLow(context, farm.id)
        // Fire high risk notification with simulated rain & warm temperature
        val sent = sendHighDiseaseRiskNotification(
            context = context,
            farm = farm,
            rainfallMm = 14.8,
            avgTempC = 27.4,
            customNotice = "SIMULATED WEATHER EVENT: 14.8 mm rainfall and 27.4°C warmth caused sudden escalation from Low to High fungal spore risk for ${farm.crop}."
        )
        // Set state to HIGH
        setPreviousRiskLevel(context, farm.id, DiseaseRiskLevel.HIGH)
        return sent
    }
}
