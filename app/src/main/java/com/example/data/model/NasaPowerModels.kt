package com.example.data.model

/**
 * Data models for AgriTwin - Climate-Twin Farming Intelligence (v3).
 * Developed by Team DevTech for NASA Space Apps Challenge 2026 ("Field Shift: Adapting Farms with NASA Data").
 * All copy and nomenclature conforms to British English (en-GB).
 */

data class DistrictLocation(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
)

val PRESET_DISTRICTS = listOf(
    DistrictLocation(
        id = "faisalabad",
        name = "Faisalabad",
        country = "Pakistan",
        latitude = 31.4187,
        longitude = 73.0791,
        description = "Agricultural heartland of Punjab — major wheat, sugarcane, and cotton belt."
    ),
    DistrictLocation(
        id = "multan",
        name = "Multan",
        country = "Pakistan",
        latitude = 30.1575,
        longitude = 71.5249,
        description = "Southern Punjab arid plain — world-renowned mango and cotton hub."
    ),
    DistrictLocation(
        id = "lahore",
        name = "Lahore",
        country = "Pakistan",
        latitude = 31.5204,
        longitude = 74.3587,
        description = "Upper Indus basin — peri-urban intensive vegetable and rice production."
    ),
    DistrictLocation(
        id = "bahawalpur",
        name = "Bahawalpur",
        country = "Pakistan",
        latitude = 29.3956,
        longitude = 71.6833,
        description = "Cholistan desert borderland — hyper-arid canal colony with heat-stressed crops."
    )
)

/**
 * Climatology data retrieved from NASA POWER API or labelled demo fallback.
 */
data class ClimatologyData(
    val meanAnnualTempC: Double,
    val meanAnnualPrecipitationMm: Double,
    val monthlyTemperatures: Map<String, Double> = emptyMap(),
    val monthlyPrecipitation: Map<String, Double> = emptyMap(),
    val isLiveNasaData: Boolean = false,
    val sourceLabel: String = if (isLiveNasaData) "Live · NASA POWER" else "Demo data",
    val elevationMeters: Double = 184.0,
    val requestUrl: String = ""
)

/**
 * Future climate projection assumptions.
 */
data class ClimateProjection(
    val temperatureIncreaseC: Double = 1.2,
    val precipitationChangePercent: Double = -5.0,
    val timeHorizonYear: Int = 2035,
    val scenarioNotes: String = "IPCC SSP2-4.5 regional adaptation trajectory for the Indus Basin."
)

/**
 * Global farming region used for Climate-Twin matching.
 */
data class GlobalTwinRegion(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val meanTempC: Double,
    val meanPrecipMm: Double,
    val agroZone: String,
    val currentSuccessRotations: List<String>,
    val adaptationPlaybook: String,
    val soilManagementAdvice: String,
    val droughtResilienceRating: String
)

/**
 * Result of matching against candidate regions.
 */
data class TwinMatchResult(
    val region: GlobalTwinRegion,
    val similarityScorePercent: Double,
    val normalizedDistance: Double,
    val deltaTempC: Double,
    val deltaPrecipMm: Double,
    val rotationPlan: List<String>
)

/**
 * Daily rainfall & meteorology record from NASA POWER.
 */
data class DailyRainfallRecord(
    val dateString: String,
    val rainfallMm: Double,
    val maxTempC: Double? = null,
    val windSpeedMs: Double? = null
)

data class DailyFetchResult(
    val records: List<DailyRainfallRecord>,
    val requestUrl: String,
    val isLive: Boolean = true
)

/**
 * Field water and soil moisture status.
 */
data class FieldWaterStatus(
    val fieldId: String,
    val fieldName: String,
    val crop: String,
    val soilMoisturePercent: Int,
    val waterStressIndex: Double, // 0.0 (optimal) to 1.0 (severe stress)
    val drainageStatus: String,
    val isDemoData: Boolean = true
)

/**
 * Weather & hazard alert.
 */
data class WeatherAlert(
    val id: String,
    val title: String,
    val severity: AlertSeverity,
    val thresholdTriggered: String,
    val description: String,
    val suggestedAction: String,
    val isLiveNasaDerived: Boolean,
    val timestamp: String,
    val requestUrl: String = ""
)

enum class AlertSeverity {
    INFO, ADVISORY, WARNING, SEVERE
}

/**
 * Soil health indicator score (0 to 100) and historical trend.
 */
data class SoilHealthScore(
    val fieldId: String,
    val fieldName: String,
    val overallScore: Int,
    val organicCarbonPercent: Double,
    val aggregateStabilityIndex: Int,
    val waterRetentionCapacityMm: Int,
    val micronutrientScore: Int,
    val historicalScores: List<Int>,
    val isDemoData: Boolean = true
)

/**
 * Saved Farm model for the headline "My Farm" feature.
 */
data class SavedFarm(
    val id: String,
    val name: String,
    val crop: String, // Wheat, Cotton, Rice, Maize, Sugarcane
    val latitude: Double,
    val longitude: Double,
    val areaAcres: Double = 25.0
)

val DEFAULT_FARMS = listOf(
    SavedFarm(
        id = "farm_chenab_faisalabad",
        name = "Chak 217 Canal Colony",
        crop = "Wheat",
        latitude = 31.4200,
        longitude = 73.1000,
        areaAcres = 40.0
    ),
    SavedFarm(
        id = "farm_vehari_multan",
        name = "Vehari Riverbank Estate",
        crop = "Cotton",
        latitude = 30.0450,
        longitude = 72.3520,
        areaAcres = 65.0
    )
)

/**
 * Disease Risk Level calculated from fetched atmospheric conditions.
 */
enum class DiseaseRiskLevel(val label: String) {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High")
}

/**
 * Weekly Satellite Report generated on demand for a specific farm.
 */
data class WeeklySatelliteReport(
    val farmId: String,
    val farmName: String,
    val generatedDate: String,
    val totalRainfallMm: Double,
    val previousWeekRainfallMm: Double,
    val deltaRainfallMm: Double,
    val avgTempC: Double,
    val maxTempC: Double,
    val heatStressDaysCount: Int, // days with temp >= 40 °C
    val vegetationHealthTrend: String,
    val irrigationAdvice: String,
    val diseaseRiskNote: String,
    val diseaseRiskLevel: DiseaseRiskLevel = DiseaseRiskLevel.LOW,
    val dailyRecords: List<DailyRainfallRecord>,
    val isLiveReport: Boolean,
    val requestUrl: String,
    val soilMoisturePercent: Double = 34.0, // NASA GLDAS / POWER root zone volumetric soil moisture (0-100%)
    val soilMoistureStatus: String = "Adequate", // Deficit (<25%), Adequate (25-45%), Saturated (>45%)
    val irrigationRequirementMm: Double = 18.0, // Net weekly crop water requirement in mm
    val gldasDataSource: String = "NASA GLDAS-2.1 / POWER Noah LSM",
    // Historical Climate Comparison (Same 7-Day Period Previous Year)
    val historicalDailyRecords: List<DailyRainfallRecord> = emptyList(),
    val historicalYearLabel: String = "2025",
    val historicalTotalRainfallMm: Double = 0.0,
    val historicalAvgTempC: Double = 0.0,
    val historicalMaxTempC: Double = 0.0,
    val yoyRainfallDeltaMm: Double = 0.0,
    val yoyTempDeltaC: Double = 0.0,
    val historicalTrendAnalysis: String = "Historical baseline aligned with multi-year seasonal norm."
)
