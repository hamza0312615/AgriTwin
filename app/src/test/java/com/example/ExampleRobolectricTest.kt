package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ClimateProjection
import com.example.data.model.ClimateTwinMatcher
import com.example.data.model.ClimatologyData
import com.example.data.model.SoilHealthEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies AgriTwin app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AgriTwin", appName)
    }

    @Test
    fun `climate twin matcher computes ordered matches with normalized distance`() {
        val currentClimatology = ClimatologyData(
            meanAnnualTempC = 24.5,
            meanAnnualPrecipitationMm = 375.0,
            isLiveNasaData = false
        )
        val projection = ClimateProjection(
            temperatureIncreaseC = 1.2,
            precipitationChangePercent = -5.0
        )

        val matches = ClimateTwinMatcher.findClimateTwins(currentClimatology, projection)
        assertTrue("Matches should not be empty", matches.isNotEmpty())
        val topMatch = matches.first()
        assertNotNull("Top match region should exist", topMatch.region)
        assertTrue("Top match similarity should be high", topMatch.similarityScorePercent > 50.0)
        assertTrue("Rotations should be provided", topMatch.rotationPlan.isNotEmpty())
    }

    @Test
    fun `soil health engine scores match documented formula weighting`() {
        // Optimal benchmark test
        val score = SoilHealthEngine.calculateScore(
            socPercent = 1.50,
            aggregateStability = 80,
            waterCapacityMm = 160,
            micronutrientScore = 100
        )
        assertEquals(100, score)

        // Moderate field test
        val fieldScore = SoilHealthEngine.calculateScore(
            socPercent = 1.0,
            aggregateStability = 60,
            waterCapacityMm = 120,
            micronutrientScore = 70
        )
        assertTrue("Score should be within reasonable bounds", fieldScore in 65..80)
    }

    @Test
    fun `disease alert notification system triggers when risk transitions from Low to High`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val farm = com.example.data.model.DEFAULT_FARMS.first()

        // Ensure baseline is LOW
        com.example.util.DiseaseAlertNotificationManager.resetFarmRiskToLow(context, farm.id)
        assertEquals(
            com.example.data.model.DiseaseRiskLevel.LOW,
            com.example.util.DiseaseAlertNotificationManager.getPreviousRiskLevel(context, farm.id)
        )

        // Case 1: Report with LOW risk should NOT trigger escalation
        val lowReport = com.example.data.model.WeeklySatelliteReport(
            farmId = farm.id,
            farmName = farm.name,
            generatedDate = "11 Oct 2026",
            totalRainfallMm = 0.0,
            previousWeekRainfallMm = 0.0,
            deltaRainfallMm = 0.0,
            avgTempC = 28.0,
            maxTempC = 32.0,
            heatStressDaysCount = 0,
            vegetationHealthTrend = "Stable",
            irrigationAdvice = "Maintain canal cycle",
            diseaseRiskNote = "Dry atmosphere suppresses spores",
            diseaseRiskLevel = com.example.data.model.DiseaseRiskLevel.LOW,
            dailyRecords = emptyList(),
            isLiveReport = true,
            requestUrl = "https://power.larc.nasa.gov"
        )
        val lowResult = com.example.util.DiseaseAlertNotificationManager.checkAndNotifyDiseaseRiskChange(
            context, farm, lowReport
        )
        assertTrue("LOW risk should not escalate", !lowResult.escalatedToHigh)

        // Case 2: Weather shifts: warm rain causes HIGH risk -> MUST escalate
        val highReport = lowReport.copy(
            totalRainfallMm = 12.5,
            diseaseRiskLevel = com.example.data.model.DiseaseRiskLevel.HIGH,
            diseaseRiskNote = "Elevated foliar humidity risk: warm temperatures combined with recent rain favour rapid fungal spore germination."
        )
        val highResult = com.example.util.DiseaseAlertNotificationManager.checkAndNotifyDiseaseRiskChange(
            context, farm, highReport
        )
        assertTrue("Transition from LOW to HIGH must escalate", highResult.escalatedToHigh)

        // Case 3: Subsequent fetch while still HIGH should NOT re-escalate
        val repeatHighResult = com.example.util.DiseaseAlertNotificationManager.checkAndNotifyDiseaseRiskChange(
            context, farm, highReport
        )
        assertTrue("Subsequent HIGH risk should not re-escalate", !repeatHighResult.escalatedToHigh)
    }

    @Test
    fun `AppLocationManager updates state and validates manual coordinates bounds`() {
        val manager = com.example.data.location.AppLocationManager

        // Valid manual coordinates
        val success = manager.setManualCoordinates(30.1575, 71.5249, "Multan Farm")
        assertTrue("Valid coordinates should succeed", success)
        assertEquals(30.1575, manager.currentLocation.value.latitude, 0.0001)
        assertEquals(71.5249, manager.currentLocation.value.longitude, 0.0001)
        assertEquals("Multan Farm", manager.currentLocation.value.displayName)

        // Invalid latitude (> 90)
        val invalidLat = manager.setManualCoordinates(95.0, 71.5249)
        assertTrue("Invalid latitude should fail", !invalidLat)

        // Invalid longitude (> 180)
        val invalidLon = manager.setManualCoordinates(30.0, 195.0)
        assertTrue("Invalid longitude should fail", !invalidLon)

        // Preset district update
        val lahore = com.example.data.model.PRESET_DISTRICTS.first { it.id == "lahore" }
        manager.setFromPresetDistrict(lahore)
        assertEquals(31.5204, manager.currentLocation.value.latitude, 0.0001)
        assertEquals(74.3587, manager.currentLocation.value.longitude, 0.0001)
    }
}
