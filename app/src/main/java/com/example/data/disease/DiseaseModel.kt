package com.example.data.disease

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * =========================================================================================
 * CROP DISEASE ANALYSER ARCHITECTURAL INTERFACE (disease-model)
 * Team DevTech — NASA Space Apps Challenge 2026 ("Field Shift: Adapting Farms with NASA Data")
 * =========================================================================================
 *
 * DESIGN CONTRACT:
 * This interface decouples leaf image acquisition from diagnosis inference.
 * In a production AI pipeline, this interface can be replaced with an on-device TensorFlow Lite,
 * PyTorch Mobile, or cloud Vision transformer model with a single code line binding change.
 *
 * For the hackathon evaluation, the HeuristicHsvDiseaseModel implementation analyses
 * HSV pixel distributions directly on the sampled bitmap.
 *
 * MANDATORY COMPLIANCE:
 * 1. Every condition diagnosis string MUST include "(demo assessment)".
 * 2. All diagnosis results carry the explicit disclaimer: "Demo mode — not a real diagnosis".
 */
interface CropDiseaseModel {
    /**
     * Analyse a leaf bitmap image and return condition diagnostics.
     */
    suspend fun analyzeLeafImage(bitmap: Bitmap): DiseaseDiagnosisResult
}

data class LeafColorDistribution(
    val greenPercent: Float,
    val yellowPercent: Float,
    val brownPercent: Float,
    val otherPercent: Float
)

data class DiseaseDiagnosisResult(
    val conditionName: String, // MUST include "(demo assessment)"
    val confidencePercent: Int,
    val colorDistribution: LeafColorDistribution,
    val primaryObservation: String,
    val treatmentSteps: List<String>,
    val preventiveMeasures: List<String>,
    val disclaimerBanner: String = "Demo mode — not a real diagnosis"
)

/**
 * Baseline HSV-based spectral colour heuristic analyzer.
 * Evaluates leaf pigmentation distributions across Hue-Saturation-Value color space.
 */
class HeuristicHsvDiseaseModel : CropDiseaseModel {

    override suspend fun analyzeLeafImage(bitmap: Bitmap): DiseaseDiagnosisResult =
        withContext(Dispatchers.Default) {
            // Downsample bitmap for fast, responsive analysis
            val sampleWidth = 120
            val sampleHeight = (bitmap.height * (120f / bitmap.width)).toInt().coerceAtLeast(60)
            val scaled = Bitmap.createScaledBitmap(bitmap, sampleWidth, sampleHeight, false)

            var greenCount = 0
            var yellowCount = 0
            var brownCount = 0
            var otherCount = 0
            val totalPixels = scaled.width * scaled.height

            val hsv = FloatArray(3)

            for (y in 0 until scaled.height) {
                for (x in 0 until scaled.width) {
                    val pixel = scaled.getPixel(x, y)
                    Color.colorToHSV(pixel, hsv)

                    val hue = hsv[0]        // 0 to 360
                    val sat = hsv[1]        // 0.0 to 1.0
                    val value = hsv[2]      // 0.0 to 1.0

                    when {
                        // Background / near white / very dark
                        value < 0.12f || (sat < 0.12f && value > 0.85f) -> {
                            otherCount++
                        }
                        // Healthy vegetative green: Hue roughly 75° to 165°
                        hue in 75.0f..165.0f && sat >= 0.20f && value >= 0.18f -> {
                            greenCount++
                        }
                        // Chlorotic / Yellowing: Hue roughly 38° to 74°
                        hue in 38.0f..74.0f && sat >= 0.25f && value >= 0.28f -> {
                            yellowCount++
                        }
                        // Necrotic / Rust / Brown: Hue 10° to 37° with lower value or moderate saturation
                        hue in 10.0f..37.0f && sat >= 0.20f && value in 0.15f..0.65f -> {
                            brownCount++
                        }
                        // Very dark brownish lesions
                        hue in 5.0f..45.0f && value < 0.35f && sat >= 0.18f -> {
                            brownCount++
                        }
                        else -> {
                            otherCount++
                        }
                    }
                }
            }

            val validVegetativePixels = (greenCount + yellowCount + brownCount).coerceAtLeast(1)
            val greenRatio = (greenCount.toFloat() / validVegetativePixels) * 100f
            val yellowRatio = (yellowCount.toFloat() / validVegetativePixels) * 100f
            val brownRatio = (brownCount.toFloat() / validVegetativePixels) * 100f
            val otherRatio = (otherCount.toFloat() / totalPixels.toFloat()) * 100f

            val distribution = LeafColorDistribution(
                greenPercent = greenRatio,
                yellowPercent = yellowRatio,
                brownPercent = brownRatio,
                otherPercent = otherRatio
            )

            // Diagnostic logic with strictly enforced "(demo assessment)" suffix
            when {
                brownRatio >= 28f -> {
                    DiseaseDiagnosisResult(
                        conditionName = "Necrotic Leaf Blight (demo assessment)",
                        confidencePercent = (75 + (brownRatio * 0.2f)).toInt().coerceIn(75, 96),
                        colorDistribution = distribution,
                        primaryObservation = "Elevated brown necrotic tissue (${brownRatio.toInt()}%) detected on leaf surface, indicative of fungal lesion expansion or thermal scorch.",
                        treatmentSteps = listOf(
                            "Prune heavily infected lower leaves and safely dispose away from watercourses.",
                            "Apply protective copper oxychloride or azoxystrobin spray during cool morning hours.",
                            "Avoid sprinkler irrigation that keeps crop foliage damp over prolonged periods."
                        ),
                        preventiveMeasures = listOf(
                            "Rotate with non-host legume crops in the succeeding seasonal cycle.",
                            "Maintain optimal plant spacing to promote canopy airflow and reduce relative humidity."
                        )
                    )
                }
                yellowRatio >= 26f && brownRatio >= 10f -> {
                    DiseaseDiagnosisResult(
                        conditionName = "Early Foliar Rust & Pustules (demo assessment)",
                        confidencePercent = 84,
                        colorDistribution = distribution,
                        primaryObservation = "Combination of chlorotic yellow halos (${yellowRatio.toInt()}%) and localized rust pustules (${brownRatio.toInt()}%) observed across vegetative blade.",
                        treatmentSteps = listOf(
                            "Apply systemic triazole fungicide (e.g. tebuconazole / propiconazole) upon first pustule emergence.",
                            "Inspect adjoining field borders and windward perimeter beds for secondary spore spread.",
                            "Avoid excess late-season nitrogen applications which promote lush, vulnerable tissue."
                        ),
                        preventiveMeasures = listOf(
                            "Deploy certified rust-resistant seed cultivars (e.g. Akbar-19, Umeed).",
                            "Monitor NASA POWER atmospheric humidity indicators during heading phase."
                        )
                    )
                }
                yellowRatio >= 22f -> {
                    DiseaseDiagnosisResult(
                        conditionName = "Nitrogen Chlorosis & Micronutrient Stress (demo assessment)",
                        confidencePercent = 88,
                        colorDistribution = distribution,
                        primaryObservation = "Diffuse yellowing (${yellowRatio.toInt()}%) concentrated along central leaf veins while cellular structure remains structurally intact.",
                        treatmentSteps = listOf(
                            "Conduct targeted foliar spray of 1.5% urea solution with zinc sulfate heptahydrate.",
                            "Check irrigation canal water salinity; high carbonates often induce lime-induced iron chlorosis.",
                            "Incorporate decomposed farmyard manure or vermicompost to stimulate microbial nitrogen cycling."
                        ),
                        preventiveMeasures = listOf(
                            "Incorporate summer green manure (Sesbania / Dhaincha) before primary crop sowing.",
                            "Utilise NDVI satellite vegetation indices to guide variable-rate topdressing."
                        )
                    )
                }
                else -> {
                    DiseaseDiagnosisResult(
                        conditionName = "Healthy Vegetative Foliage (demo assessment)",
                        confidencePercent = 94,
                        colorDistribution = distribution,
                        primaryObservation = "Vibrant photosynthetic green index (${greenRatio.toInt()}%) with minimal chlorosis or necrotic tissue detected.",
                        treatmentSteps = listOf(
                            "Continue standard integrated pest and nutrient management schedule.",
                            "Maintain balanced soil moisture as indicated on the AgriTwin Water Dashboard."
                        ),
                        preventiveMeasures = listOf(
                            "Regularly inspect undersides of leaves for early aphid or whitefly nymph colonies.",
                            "Ensure consistent mulching to insulate rootzone biology."
                        )
                    )
                }
            }
        }
}
