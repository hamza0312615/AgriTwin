package com.example.data.model

/**
 * =========================================================================================
 * SOIL HEALTH SCORING FORMULA & BENCHMARK ENGINE
 * Team DevTech — NASA Space Apps Challenge 2026 ("Field Shift: Adapting Farms with NASA Data")
 * =========================================================================================
 *
 * MATHEMATICAL SCORING FORMULA:
 * The AgriTwin Soil Health Index (SHI, scaled 0 to 100) integrates four biophysical pillars:
 *
 *   SHI = (w_soc * S_soc) + (w_as * S_as) + (w_whc * S_whc) + (w_mn * S_mn)
 *
 * Parameter Weights (normalized sum = 1.00):
 *   - w_soc = 0.35 : Soil Organic Carbon (SOC) — central driver of biological activity and nutrient retention.
 *   - w_as  = 0.25 : Aggregate Stability (AS) — resistance to slaking, crusting, and erosion under heavy rain.
 *   - w_whc = 0.25 : Water Holding Capacity (WHC) — capillary water reservoir buffering dry spells.
 *   - w_mn  = 0.15 : Micronutrient Balance (MN) — availability of Zn, B, Fe, and active mineral nitrogen.
 *
 * Sub-Score Normalisation Functions:
 *   1. S_soc = min(100, (SOC_percent / 1.50) * 100)  [Where 1.50% SOC is considered optimal for arid/semi-arid Indus soils]
 *   2. S_as  = min(100, (AS_index / 80) * 100)
 *   3. S_whc = min(100, (WHC_mm / 160) * 100)
 *   4. S_mn  = Micronutrient Index (0 to 100 based on composite elemental test assays)
 *
 * DATA HONESTY:
 * All field soil evaluations are badged "Demo data" in accordance with hackathon criteria.
 */
object SoilHealthEngine {

    const val WEIGHT_SOC = 0.35
    const val WEIGHT_AGGREGATE_STABILITY = 0.25
    const val WEIGHT_WATER_HOLDING = 0.25
    const val WEIGHT_MICRONUTRIENTS = 0.15

    fun calculateScore(
        socPercent: Double,
        aggregateStability: Int,
        waterCapacityMm: Int,
        micronutrientScore: Int
    ): Int {
        val sSoc = (socPercent / 1.50 * 100.0).coerceIn(0.0, 100.0)
        val sAs = (aggregateStability.toDouble() / 80.0 * 100.0).coerceIn(0.0, 100.0)
        val sWhc = (waterCapacityMm.toDouble() / 160.0 * 100.0).coerceIn(0.0, 100.0)
        val sMn = micronutrientScore.toDouble().coerceIn(0.0, 100.0)

        val total = (WEIGHT_SOC * sSoc) +
                (WEIGHT_AGGREGATE_STABILITY * sAs) +
                (WEIGHT_WATER_HOLDING * sWhc) +
                (WEIGHT_MICRONUTRIENTS * sMn)

        return total.toInt().coerceIn(0, 100)
    }

    val SAMPLE_FIELD_SCORES: List<SoilHealthScore> = listOf(
        SoilHealthScore(
            fieldId = "field_f1",
            fieldName = "Field 1 · Main Alluvial Basin",
            overallScore = 84,
            organicCarbonPercent = 1.32,
            aggregateStabilityIndex = 72,
            waterRetentionCapacityMm = 142,
            micronutrientScore = 86,
            historicalScores = listOf(68, 71, 74, 78, 81, 84),
            isDemoData = true
        ),
        SoilHealthScore(
            fieldId = "field_f2",
            fieldName = "Field 2 · Canal Tail-End Parcel",
            overallScore = 61,
            organicCarbonPercent = 0.74,
            aggregateStabilityIndex = 52,
            waterRetentionCapacityMm = 105,
            micronutrientScore = 65,
            historicalScores = listOf(55, 54, 58, 59, 60, 61),
            isDemoData = true
        ),
        SoilHealthScore(
            fieldId = "field_f3",
            fieldName = "Field 3 · Regenerative Legume Trial",
            overallScore = 92,
            organicCarbonPercent = 1.48,
            aggregateStabilityIndex = 78,
            waterRetentionCapacityMm = 155,
            micronutrientScore = 91,
            historicalScores = listOf(70, 75, 80, 85, 89, 92),
            isDemoData = true
        ),
        SoilHealthScore(
            fieldId = "field_f4",
            fieldName = "Field 4 · Saline-Affected Perimeter",
            overallScore = 48,
            organicCarbonPercent = 0.52,
            aggregateStabilityIndex = 41,
            waterRetentionCapacityMm = 82,
            micronutrientScore = 49,
            historicalScores = listOf(44, 45, 46, 45, 47, 48),
            isDemoData = true
        )
    )
}
