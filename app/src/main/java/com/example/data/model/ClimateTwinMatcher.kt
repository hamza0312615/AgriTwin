package com.example.data.model

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Climate Twin Matcher Engine.
 * Team DevTech — NASA Space Apps Challenge 2026.
 *
 * MATHEMATICAL MATCHING FORMULA:
 * Given a target location with projected mean annual temperature T_target (°C)
 * and mean annual precipitation P_target (mm/year):
 *
 * 1. Normalized Euclidean Distance:
 *    Distance = sqrt( ((T_target - T_candidate) / sigma_T)^2 + ((P_target - P_candidate) / sigma_P)^2 )
 *    where:
 *      sigma_T = 6.5 °C (standard deviation of agricultural temperature distribution across arid/semi-arid farming zones)
 *      sigma_P = 250.0 mm (standard deviation of agricultural annual rainfall distribution across benchmark zones)
 *
 * 2. Similarity Metric (0% to 100%):
 *    Similarity = exp( -0.5 * Distance ) * 100.0
 *
 * This metric ensures smooth decay, penalises extreme divergences in either thermal or hygrometric
 * regimes, and provides a normalised similarity score suitable for agricultural decision support.
 */
object ClimateTwinMatcher {

    private const val SIGMA_TEMP = 6.5 // °C standard deviation scale factor
    private const val SIGMA_PRECIP = 250.0 // mm standard deviation scale factor

    // Curated benchmark agricultural zones around the globe with established rotation regimes
    val GLOBAL_REGIONS: List<GlobalTwinRegion> = listOf(
        GlobalTwinRegion(
            id = "al_qassim",
            name = "Al-Qassim Oasis",
            country = "Saudi Arabia",
            latitude = 26.2167,
            longitude = 43.9833,
            meanTempC = 26.8,
            meanPrecipMm = 135.0,
            agroZone = "Hyper-arid irrigated oasis",
            currentSuccessRotations = listOf("Date Palm Agroforestry", "Heat-Tolerant Wheat (Yecora Rojo)", "Forage Sorghum"),
            adaptationPlaybook = "Farmers in Al-Qassim overcome extreme heat stress using date-palm canopy shading with intercropped high-yield Yecora Rojo wheat and saline-tolerant sorghum rotations. Adopting subsurface drip fertigation reduces transpiration loss by 42%.",
            soilManagementAdvice = "Gypsum soil conditioning with localized biochar trenches to lock soil moisture.",
            droughtResilienceRating = "Tier 1: Exceptional Hyper-Arid Resilience"
        ),
        GlobalTwinRegion(
            id = "punjab_ludhiana",
            name = "Ludhiana Plains",
            country = "India",
            latitude = 30.9010,
            longitude = 75.8573,
            meanTempC = 24.3,
            meanPrecipMm = 680.0,
            agroZone = "Indo-Gangetic alluvial plain",
            currentSuccessRotations = listOf("Direct-Seeded Rice", "Conservation Wheat", "Summer Mungbean (Green Gram)"),
            adaptationPlaybook = "Farmers in Ludhiana have pioneered short-duration direct-seeded rice (DSR) paired with relay-cropped summer mungbean. This fixes atmospheric nitrogen, cuts 3 canal irrigations, and prepares the seedbed for zero-till wheat.",
            soilManagementAdvice = "Laser land levelling combined with happy seeder residue retention prevents crusting.",
            droughtResilienceRating = "Tier 2: High Alluvial Adaptation"
        ),
        GlobalTwinRegion(
            id = "nile_delta",
            name = "Kafr El-Sheikh, Nile Delta",
            country = "Egypt",
            latitude = 31.1107,
            longitude = 30.9388,
            meanTempC = 21.4,
            meanPrecipMm = 180.0,
            agroZone = "Mediterranean deltaic arid",
            currentSuccessRotations = listOf("Egyptian Cotton (Giza)", "Grain Maize", "Berseem Egyptian Clover"),
            adaptationPlaybook = "Farmers in the Nile Delta utilise dense winter Berseem clover rotations that restore soil organic matter after intensive summer maize and cotton, buffering salt intrusion and thermal shocks.",
            soilManagementAdvice = "Tile drainage systems with periodic sulfur flushes to suppress sodicity.",
            droughtResilienceRating = "Tier 2: Strong Salinity-Heat Coping"
        ),
        GlobalTwinRegion(
            id = "san_joaquin",
            name = "San Joaquin Valley, California",
            country = "United States",
            latitude = 36.7468,
            longitude = -119.7726,
            meanTempC = 17.8,
            meanPrecipMm = 290.0,
            agroZone = "Semi-arid Mediterranean valley",
            currentSuccessRotations = listOf("Winter Wheat Cover", "Processing Tomatoes", "Silage Sorghum"),
            adaptationPlaybook = "Californian growers facing groundwater limits replaced double-cropped maize with deficit-irrigated sorghum and winter cereal cover crops, cutting water duty while protecting fragile topsoil.",
            soilManagementAdvice = "Regenerative minimum tillage with continuous winter legume cover mulching.",
            droughtResilienceRating = "Tier 1: Advanced Precision Water Control"
        ),
        GlobalTwinRegion(
            id = "murray_darling",
            name = "Riverina, Murray-Darling",
            country = "Australia",
            latitude = -34.5000,
            longitude = 146.0000,
            meanTempC = 17.2,
            meanPrecipMm = 410.0,
            agroZone = "Semi-arid temperate interior",
            currentSuccessRotations = listOf("Drought-Bred Barley", "Canola", "Chickpea Pulses"),
            adaptationPlaybook = "Riverina growers rely on early-sowing winter canola followed by deep-rooted chickpea rotations. The pulse crop breaks cereal disease cycles and leaves residual nitrogen for subsequent barley crops.",
            soilManagementAdvice = "Stubble retention and deep ripping with lime to manage subsoil acidity.",
            droughtResilienceRating = "Tier 1: Robust Variable-Rainfall Mastery"
        ),
        GlobalTwinRegion(
            id = "konya_basin",
            name = "Konya Basin Steppe",
            country = "Turkey",
            latitude = 37.8746,
            longitude = 32.4932,
            meanTempC = 12.1,
            meanPrecipMm = 325.0,
            agroZone = "Anatolian semi-arid plateau",
            currentSuccessRotations = listOf("Winter Durum Wheat", "Sugar Beet", "Desi Chickpeas"),
            adaptationPlaybook = "Konya farmers alternate cold-hardy durum wheat with dryland chickpeas and targeted drip-irrigated sugar beet, maintaining yield stability under erratic spring rains.",
            soilManagementAdvice = "Contour contouring and fallow green manure Incorporation.",
            droughtResilienceRating = "Tier 2: Continental Thermal Resilience"
        ),
        GlobalTwinRegion(
            id = "mendoza",
            name = "Cuyo Basin, Mendoza",
            country = "Argentina",
            latitude = -32.8895,
            longitude = -68.8458,
            meanTempC = 16.5,
            meanPrecipMm = 220.0,
            agroZone = "Andean rain-shadow arid",
            currentSuccessRotations = listOf("Alfalfa Meadow", "Grain Maize", "Winter Rye"),
            adaptationPlaybook = "Argentine Andean farmers sequence multi-year alfalfa stands with short-season grain maize to maximize glacier-fed runoff windows and stabilize sandy soils against desiccating zonda winds.",
            soilManagementAdvice = "Deep-root alfalfa biologically breaks compaction and fixes over 150 kg N/ha.",
            droughtResilienceRating = "Tier 2: Arid Runoff Harmonization"
        ),
        GlobalTwinRegion(
            id = "sahel_maradi",
            name = "Maradi Farming Belt",
            country = "Niger",
            latitude = 13.5000,
            longitude = 7.1000,
            meanTempC = 28.5,
            meanPrecipMm = 480.0,
            agroZone = "Sahelian semi-arid savanna",
            currentSuccessRotations = listOf("Pearl Millet", "Intercropped Cowpea", "Dual-Purpose Sorghum"),
            adaptationPlaybook = "Sahelian producers utilise farmer-managed natural regeneration (FMNR) intercropped with pearl millet and trailing cowpeas. Cowpea foliage shades the ground and suppresses thermal evaporation by up to 35%.",
            soilManagementAdvice = "Zaï planting pits filled with composted organic manure to capture ephemeral rainfall.",
            droughtResilienceRating = "Tier 1: Extreme Thermal and Drought Survival"
        ),
        GlobalTwinRegion(
            id = "alentejo",
            name = "Alentejo Plain",
            country = "Portugal",
            latitude = 38.5714,
            longitude = -7.9135,
            meanTempC = 16.9,
            meanPrecipMm = 585.0,
            agroZone = "Iberian dry Mediterranean",
            currentSuccessRotations = listOf("Durum Wheat", "Sunflower", "Faba Bean (Broad Bean)"),
            adaptationPlaybook = "Alentejo farmers combine winter faba beans with deep-taproot sunflowers that harvest moisture from subsoil horizons during hot, dry Iberian summers.",
            soilManagementAdvice = "Bio-mulching with pruned biomass to insulate soil biology from summer heat.",
            droughtResilienceRating = "Tier 2: Mediterranean Heat Adaptation"
        ),
        GlobalTwinRegion(
            id = "central_valley_chile",
            name = "Curicó, Central Valley",
            country = "Chile",
            latitude = -34.9854,
            longitude = -71.2394,
            meanTempC = 14.8,
            meanPrecipMm = 650.0,
            agroZone = "Temperate Mediterranean valley",
            currentSuccessRotations = listOf("Maize", "Common Beans (Frijol)", "Winter Oats"),
            adaptationPlaybook = "Chilean farmers utilise high-density bean intercropping following winter oats, safeguarding humus and moderating daytime surface temperatures.",
            soilManagementAdvice = "Humic acid liquid amendments applied through microsprinklers.",
            droughtResilienceRating = "Tier 2: High Productivity Temperate Twin"
        ),
        GlobalTwinRegion(
            id = "khorasan",
            name = "Neyshabur Plain, Khorasan",
            country = "Iran",
            latitude = 36.2133,
            longitude = 58.7958,
            meanTempC = 14.2,
            meanPrecipMm = 235.0,
            agroZone = "Iranian semi-arid highland",
            currentSuccessRotations = listOf("Winter Barley", "Saffron Agro-system", "Pistachio Cover"),
            adaptationPlaybook = "Khorasan farmers run autumn-blooming saffron alongside winter barley. Saffron requires minimal spring irrigation when water supplies dwindle, shielding farmers from total drought failure.",
            soilManagementAdvice = "Qanat-inspired micro-furrows with straw shielding.",
            droughtResilienceRating = "Tier 1: Hyper-Deficit Economic Resilience"
        ),
        GlobalTwinRegion(
            id = "marathwada",
            name = "Aurangabad / Marathwada",
            country = "India",
            latitude = 19.8762,
            longitude = 75.3433,
            meanTempC = 26.2,
            meanPrecipMm = 720.0,
            agroZone = "Deccan semi-arid black cotton tract",
            currentSuccessRotations = listOf("Soybean", "Pigeon Pea (Arhar / Tur)", "Desi Cotton"),
            adaptationPlaybook = "Marathwada farmers successfully pair short-cycle soybean with long-duration pigeon pea in a 4:2 row geometry. If late monsoon fails, the deep-rooted pigeon pea continues to mature and yields reliably.",
            soilManagementAdvice = "Broad-bed furrow (BBF) systems in Vertisol soils prevent waterlogging and conserve soil water.",
            droughtResilienceRating = "Tier 2: Monsoon-Intermittent Resilience"
        )
    )

    /**
     * Compute matches ordered by proximity to the projected target climate.
     */
    fun findClimateTwins(
        currentClimatology: ClimatologyData,
        projection: ClimateProjection
    ): List<TwinMatchResult> {
        val targetTemp = currentClimatology.meanAnnualTempC + projection.temperatureIncreaseC
        val precipMultiplier = (100.0 + projection.precipitationChangePercent) / 100.0
        val targetPrecip = (currentClimatology.meanAnnualPrecipitationMm * precipMultiplier).coerceAtLeast(10.0)

        return GLOBAL_REGIONS.map { region ->
            val tempDiff = targetTemp - region.meanTempC
            val precipDiff = targetPrecip - region.meanPrecipMm

            val normalizedTempComponent = (tempDiff / SIGMA_TEMP).pow(2.0)
            val normalizedPrecipComponent = (precipDiff / SIGMA_PRECIP).pow(2.0)

            // Normalized Euclidean distance
            val distance = sqrt(normalizedTempComponent + normalizedPrecipComponent)

            // Similarity score decaying exponentially with distance
            val similarity = (exp(-0.45 * distance) * 100.0).coerceIn(12.0, 99.4)

            TwinMatchResult(
                region = region,
                similarityScorePercent = similarity,
                normalizedDistance = distance,
                deltaTempC = tempDiff,
                deltaPrecipMm = precipDiff,
                rotationPlan = region.currentSuccessRotations
            )
        }.sortedByDescending { it.similarityScorePercent }
    }
}
