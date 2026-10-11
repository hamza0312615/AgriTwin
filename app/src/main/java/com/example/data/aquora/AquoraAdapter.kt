package com.example.data.aquora

import com.example.data.model.FieldWaterStatus

/**
 * =========================================================================================
 * AQUORA SATELLITE WATER-MONITORING PIPELINE ADAPTER (aquora-adapter)
 * Team DevTech — NASA Space Apps Challenge 2026 ("Field Shift: Adapting Farms with NASA Data")
 * =========================================================================================
 *
 * ARCHITECTURAL PURPOSE & INTEGRATION CONTRACT:
 * This module serves as the production plug-in point for Team DevTech's proprietary
 * AQUORA (Automated Quantitative Water Observation & Regional Allocation) satellite
 * water-monitoring pipeline.
 *
 * In production deployment, this adapter ingests multi-spectral satellite imagery and
 * microwave soil moisture data from:
 * 1. NASA SMAP (Soil Moisture Active Passive) L3/L4 radiometer retrievals (9 km / 1 km downscaled)
 * 2. NASA ECOSTRESS (ECOsystem Spaceborne Thermal Radiometer Experiment on Space Station)
 *    Evaporative Stress Index (ESI) & Water Use Efficiency (WUE)
 * 3. Sentinel-2 / Landsat-9 Normalized Difference Moisture Index (NDMI)
 *
 * For the hackathon demonstration, this adapter provides structured telemetry fields
 * and returns demo indices badged "Demo data", while exposing the clean interface for
 * live telemetry ingestion.
 */
interface AquoraSatellitePipeline {
    /**
     * Retrieve field-scale water balance and rootzone moisture metrics.
     */
    suspend fun getFieldWaterStatuses(districtId: String): List<FieldWaterStatus>

    /**
     * Synthesize plain-language irrigation recommendations based on satellite evapotranspiration
     * deficit and NASA POWER precipitation forecast.
     */
    fun synthesizeIrrigationAdvice(
        fieldStatuses: List<FieldWaterStatus>,
        recentRainfallMm: Double,
        upcomingRainExpected: Boolean
    ): IrrigationAdvice
}

data class IrrigationAdvice(
    val headline: String,
    val recommendationSummary: String,
    val actionCategory: IrrigationActionType,
    val waterVolumeSavingCubicMetersPerHectare: Int,
    val isDemoData: Boolean = true
)

enum class IrrigationActionType {
    SKIP_IRRIGATION,
    DEFICIT_IRRIGATION,
    STANDARD_APPLICATION,
    DRAINAGE_ALERT
}

/**
 * Concrete implementation of the AquoraAdapter providing baseline demo data
 * clearly badged for the Space Apps evaluation session.
 */
class AquoraAdapter : AquoraSatellitePipeline {

    override suspend fun getFieldWaterStatuses(districtId: String): List<FieldWaterStatus> {
        return listOf(
            FieldWaterStatus(
                fieldId = "field_north_4",
                fieldName = "North Sector 4 (Canal Colony)",
                crop = "Winter Wheat (Galaxy-13)",
                soilMoisturePercent = 48,
                waterStressIndex = 0.22,
                drainageStatus = "Adequate (Sandy Clay Loam)",
                isDemoData = true
            ),
            FieldWaterStatus(
                fieldId = "field_east_2",
                fieldName = "East Plot 2B (Tail-End)",
                crop = "Cotton (FH-142)",
                soilMoisturePercent = 29,
                waterStressIndex = 0.68,
                drainageStatus = "Low moisture / Deficit zone",
                isDemoData = true
            ),
            FieldWaterStatus(
                fieldId = "field_orchard_1",
                fieldName = "Citrus Grove Block A",
                crop = "Kinnow Mandarin",
                soilMoisturePercent = 54,
                waterStressIndex = 0.15,
                drainageStatus = "Optimal Rootzone Aeration",
                isDemoData = true
            ),
            FieldWaterStatus(
                fieldId = "field_south_7",
                fieldName = "South Basin Plot 7",
                crop = "Forage Sorghum / Maize",
                soilMoisturePercent = 38,
                waterStressIndex = 0.45,
                drainageStatus = "Moderate Moisture Reservoir",
                isDemoData = true
            )
        )
    }

    override fun synthesizeIrrigationAdvice(
        fieldStatuses: List<FieldWaterStatus>,
        recentRainfallMm: Double,
        upcomingRainExpected: Boolean
    ): IrrigationAdvice {
        val avgMoisture = fieldStatuses.map { it.soilMoisturePercent }.average()

        return when {
            upcomingRainExpected || recentRainfallMm >= 12.0 -> {
                IrrigationAdvice(
                    headline = "Skip irrigation this week — rain expected",
                    recommendationSummary = "NASA POWER observations indicate recent and forecasted precipitation events. Rootzone moisture is preserved across sectors; skipping canal tubewell operation will prevent waterlogging and conserve pump fuel.",
                    actionCategory = IrrigationActionType.SKIP_IRRIGATION,
                    waterVolumeSavingCubicMetersPerHectare = 450,
                    isDemoData = true
                )
            }
            avgMoisture < 35.0 -> {
                IrrigationAdvice(
                    headline = "Priority night-time deficit irrigation required",
                    recommendationSummary = "Average rootzone moisture has fallen to ${avgMoisture.toInt()}%. AQUORA evapotranspiration index indicates stress on East Plot 2B. Apply 40 mm pulse during night hours (22:00–04:00) to minimise solar evaporation losses.",
                    actionCategory = IrrigationActionType.DEFICIT_IRRIGATION,
                    waterVolumeSavingCubicMetersPerHectare = 220,
                    isDemoData = true
                )
            }
            else -> {
                IrrigationAdvice(
                    headline = "Maintain regular moisture interval — optimal conditions",
                    recommendationSummary = "Soil moisture balance is balanced at ${avgMoisture.toInt()}%. Normal capillary retention is supporting vegetative development. Next scheduled canal turn is recommended in 5 days.",
                    actionCategory = IrrigationActionType.STANDARD_APPLICATION,
                    waterVolumeSavingCubicMetersPerHectare = 120,
                    isDemoData = true
                )
            }
        }
    }
}
