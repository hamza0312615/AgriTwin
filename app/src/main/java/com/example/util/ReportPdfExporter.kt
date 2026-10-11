package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.DiseaseRiskLevel
import com.example.data.model.SavedFarm
import com.example.data.model.WeeklySatelliteReport
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates and shares beautiful, professional PDF reports of the Weekly Satellite Report
 * using Android's native android.graphics.pdf.PdfDocument (no external dependencies required).
 */
object ReportPdfExporter {

    private const val TAG = "ReportPdfExporter"
    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points (72 dpi)

    /**
     * Generates a PDF file for the given [report] and [farm], saves it to cache,
     * and triggers Android's system share sheet (e.g. WhatsApp, Email, Drive).
     *
     * @return Result containing generated File or exception
     */
    fun generateAndSharePdf(
        context: Context,
        report: WeeklySatelliteReport,
        farm: SavedFarm
    ): File? {
        val pdfFile = generatePdfFile(context, report, farm) ?: return null

        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "AgriTwin Weekly Satellite Report - ${farm.name}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the AgriTwin NASA Satellite Agrometeorological Report for ${farm.name} (${farm.crop.replaceFirstChar { it.uppercase() }}), generated on ${report.generatedDate}."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Weekly Farm Report PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            return pdfFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share PDF", e)
            Toast.makeText(context, "Could not open share sheet: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return pdfFile
        }
    }

    /**
     * Writes the PDF document to disk and returns the File.
     */
    fun generatePdfFile(
        context: Context,
        report: WeeklySatelliteReport,
        farm: SavedFarm
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            renderReportOnCanvas(canvas, report, farm)
            pdfDocument.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
            val safeName = farm.name.replace("\\s+".toRegex(), "_").filter { it.isLetterOrDigit() || it == '_' }
            val outFile = File(reportsDir, "AgriTwin_Report_${safeName}_$timeStamp.pdf")

            FileOutputStream(outFile).use { out ->
                pdfDocument.writeTo(out)
            }
            return outFile
        } catch (e: IOException) {
            Log.e(TAG, "Error generating PDF file", e)
            return null
        } finally {
            pdfDocument.close()
        }
    }

    private fun renderReportOnCanvas(
        canvas: Canvas,
        report: WeeklySatelliteReport,
        farm: SavedFarm
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.rgb(252, 253, 250) // Crisp sunlit paper
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Top Accent Banner (Agri Green Header)
        paint.color = Color.rgb(27, 94, 32) // Forest green #1B5E20
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 90f, paint)

        // Gold decorative stripe
        paint.color = Color.rgb(245, 158, 11) // Amber gold #F59E0B
        canvas.drawRect(0f, 90f, PAGE_WIDTH.toFloat(), 95f, paint)

        // Header Title: AgriTwin
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        canvas.drawText("AgriTwin · Satellite Telemetry & Crop Advisory", 36f, 42f, paint)

        // Header Subtitle
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.rgb(220, 245, 220)
        canvas.drawText("NASA POWER Climatology & Daily Agroclimatology Point Telemetry Report", 36f, 62f, paint)
        canvas.drawText("Verified Keyless Earth Observation · WGS84 Datum", 36f, 76f, paint)

        // Report Meta Card (Farm Details)
        var y = 120f
        paint.color = Color.rgb(241, 248, 233) // Light green container
        val metaRect = RectF(36f, y, PAGE_WIDTH - 36f, y + 65f)
        canvas.drawRoundRect(metaRect, 8f, 8f, paint)

        // Border around meta card
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(197, 225, 165)
        canvas.drawRoundRect(metaRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Farm Name & Details text
        paint.color = Color.rgb(27, 94, 32)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        canvas.drawText("FARM: ${farm.name.uppercase(Locale.UK)}", 50f, y + 26f, paint)

        paint.color = Color.rgb(71, 85, 105) // Slate
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10.5f
        canvas.drawText(
            "Target Crop: ${farm.crop.replaceFirstChar { it.uppercase() }}   |   Coordinates: ${String.format(Locale.UK, "%.4f°N, %.4f°E", farm.latitude, farm.longitude)}",
            50f,
            y + 44f,
            paint
        )
        canvas.drawText(
            "Report Date: ${report.generatedDate}   |   Verified: Live NASA POWER API Response",
            50f,
            y + 57f,
            paint
        )

        // KPI Matrix (3 Cards side by side)
        y += 85f
        val cardWidth = (PAGE_WIDTH - 72f - 24f) / 3f
        val cardHeight = 65f

        // Card 1: Rain
        val r1 = RectF(36f, y, 36f + cardWidth, y + cardHeight)
        paint.color = Color.rgb(224, 242, 254) // Sky light
        canvas.drawRoundRect(r1, 8f, 8f, paint)
        paint.color = Color.rgb(2, 132, 199) // Sky Blue
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("TOTAL RAINFALL (7D)", 48f, y + 20f, paint)
        paint.textSize = 17f
        canvas.drawText("${String.format(Locale.UK, "%.1f", report.totalRainfallMm)} mm", 48f, y + 42f, paint)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        val deltaSign = if (report.deltaRainfallMm >= 0) "+ " else ""
        canvas.drawText("$deltaSign${String.format(Locale.UK, "%.1f", report.deltaRainfallMm)} mm vs prev week", 48f, y + 55f, paint)

        // Card 2: Temperature
        val r2 = RectF(36f + cardWidth + 12f, y, 36f + cardWidth * 2 + 12f, y + cardHeight)
        paint.color = Color.rgb(254, 243, 199) // Sun Gold container
        canvas.drawRoundRect(r2, 8f, 8f, paint)
        paint.color = Color.rgb(180, 83, 9) // Amber text
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("AVG / MAX TEMPERATURE", 36f + cardWidth + 24f, y + 20f, paint)
        paint.textSize = 17f
        canvas.drawText(
            "${String.format(Locale.UK, "%.1f", report.avgTempC)}° / ${String.format(Locale.UK, "%.1f", report.maxTempC)}°C",
            36f + cardWidth + 24f,
            y + 42f,
            paint
        )
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("${report.heatStressDaysCount} heat stress days (≥40°C)", 36f + cardWidth + 24f, y + 55f, paint)

        // Card 3: Disease Risk
        val r3 = RectF(36f + cardWidth * 2 + 24f, y, PAGE_WIDTH - 36f, y + cardHeight)
        val (bgCol, txtCol, borderCol) = when (report.diseaseRiskLevel) {
            DiseaseRiskLevel.HIGH -> Triple(Color.rgb(254, 242, 242), Color.rgb(220, 38, 38), Color.rgb(254, 202, 202))
            DiseaseRiskLevel.MODERATE -> Triple(Color.rgb(254, 243, 199), Color.rgb(180, 83, 9), Color.rgb(253, 230, 138))
            DiseaseRiskLevel.LOW -> Triple(Color.rgb(240, 253, 244), Color.rgb(22, 163, 74), Color.rgb(187, 247, 208))
        }
        paint.color = bgCol
        canvas.drawRoundRect(r3, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = borderCol
        canvas.drawRoundRect(r3, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = txtCol
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("DISEASE RISK LEVEL", 36f + cardWidth * 2 + 36f, y + 20f, paint)
        paint.textSize = 17f
        canvas.drawText("${report.diseaseRiskLevel.label.uppercase(Locale.UK)} RISK", 36f + cardWidth * 2 + 36f, y + 42f, paint)
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("Foliar microclimate score", 36f + cardWidth * 2 + 36f, y + 55f, paint)

        // Agronomic Guidance Box
        y += 85f
        val adviceRect = RectF(36f, y, PAGE_WIDTH - 36f, y + 148f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(adviceRect, 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(adviceRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("Agrometeorological Directives & Insights", 50f, y + 22f, paint)

        // Irrigation directive
        paint.color = Color.rgb(2, 132, 199)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("• Irrigation Directive:", 50f, y + 42f, paint)
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, report.irrigationAdvice, 160f, y + 42f, PAGE_WIDTH - 190f, 13f)

        // Disease note
        paint.color = if (report.diseaseRiskLevel == DiseaseRiskLevel.HIGH) Color.rgb(220, 38, 38) else Color.rgb(22, 163, 74)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("• Pathology Note:", 50f, y + 64f, paint)
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, report.diseaseRiskNote, 160f, y + 64f, PAGE_WIDTH - 190f, 13f)

        // Vegetation trend note
        paint.color = Color.rgb(27, 94, 32)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("• Canopy Health:", 50f, y + 86f, paint)
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, report.vegetationHealthTrend, 160f, y + 86f, PAGE_WIDTH - 190f, 13f)

        // NASA GLDAS Soil moisture
        paint.color = Color.rgb(180, 83, 9)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("• Soil Moisture:", 50f, y + 108f, paint)
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, "Root profile saturation: ${String.format(Locale.UK, "%.1f", report.soilMoisturePercent)}% (${report.soilMoistureStatus}) · Irrigation deficit: ${String.format(Locale.UK, "%.0f", report.irrigationRequirementMm)} mm", 160f, y + 108f, PAGE_WIDTH - 190f, 13f)

        // YoY Historical Climate Trend
        paint.color = Color.rgb(109, 40, 217) // Purple/Violet for historical trend
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("• YoY Trend (${report.historicalYearLabel}):", 50f, y + 130f, paint)
        paint.color = Color.rgb(51, 65, 85)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        drawWrappedText(canvas, paint, "Rain Δ ${String.format(Locale.UK, "%+.1f", report.yoyRainfallDeltaMm)} mm | Temp Δ ${String.format(Locale.UK, "%+.1f", report.yoyTempDeltaC)}°C · ${report.historicalTrendAnalysis}", 160f, y + 130f, PAGE_WIDTH - 190f, 13f)

        // Daily Telemetry Table
        y += 162f
        paint.color = Color.rgb(15, 23, 42)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("7-Day Daily Telemetry Observations with Historical Overlay (NASA POWER)", 36f, y, paint)

        y += 10f
        val tableTop = y
        val colWidths = floatArrayOf(105f, 115f, 115f, 95f, 95f)
        val colHeaders = arrayOf("Date", "Precip (Live / '25)", "Temp (Live / '25)", "Wind Speed", "Telemetry")

        // Header row
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(36f, tableTop, PAGE_WIDTH - 36f, tableTop + 24f, paint)
        paint.color = Color.rgb(30, 41, 59)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f

        var curX = 46f
        for (i in colHeaders.indices) {
            canvas.drawText(colHeaders[i], curX, tableTop + 16f, paint)
            curX += colWidths[i]
        }

        y = tableTop + 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f

        for (idx in report.dailyRecords.indices) {
            val record = report.dailyRecords[idx]
            val histRecord = report.historicalDailyRecords.getOrNull(idx)
            val rowH = 22f

            paint.color = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
            canvas.drawRect(36f, y, PAGE_WIDTH - 36f, y + rowH, paint)

            // Border bottom
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            paint.color = Color.rgb(226, 232, 240)
            canvas.drawLine(36f, y + rowH, PAGE_WIDTH - 36f, y + rowH, paint)
            paint.style = Paint.Style.FILL

            // Text cells
            var cellX = 46f

            // Date
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawText(record.dateString, cellX, y + 15f, paint)
            cellX += colWidths[0]

            // Rain (Live / Historical)
            paint.color = Color.rgb(2, 132, 199)
            val histRainStr = histRecord?.let { " / ${String.format(Locale.UK, "%.1f", it.rainfallMm)}" } ?: ""
            canvas.drawText("${String.format(Locale.UK, "%.1f", record.rainfallMm)}$histRainStr mm", cellX, y + 15f, paint)
            cellX += colWidths[1]

            // Temp (Live / Historical)
            paint.color = Color.rgb(180, 83, 9)
            val histTempStr = histRecord?.let { " / ${String.format(Locale.UK, "%.1f", it.maxTempC ?: 0.0)}" } ?: ""
            canvas.drawText("${String.format(Locale.UK, "%.1f", record.maxTempC ?: 0.0)}$histTempStr °C", cellX, y + 15f, paint)
            cellX += colWidths[2]

            // Wind
            paint.color = Color.rgb(71, 85, 105)
            canvas.drawText("${String.format(Locale.UK, "%.1f", record.windSpeedMs ?: 0.0)} m/s", cellX, y + 15f, paint)
            cellX += colWidths[3]

            // Status
            paint.color = Color.rgb(22, 163, 74)
            canvas.drawText("NASA Verified", cellX, y + 15f, paint)

            y += rowH
        }

        // Table outer border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawRect(36f, tableTop, PAGE_WIDTH - 36f, y, paint)
        paint.style = Paint.Style.FILL

        // Footer / Disclaimer
        val footerY = PAGE_HEIGHT - 45f
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(
            "Telemetry Source: NASA POWER (Prediction of Worldwide Energy Resources) Agroclimatology Daily API.",
            36f,
            footerY,
            paint
        )
        canvas.drawText(
            "Generated by AgriTwin Digital Twin Platform · Shareable with agronomists, farming cooperatives, and research institutions.",
            36f,
            footerY + 12f,
            paint
        )
        canvas.drawText(
            "Coordinates verified: WGS84 (${farm.latitude}, ${farm.longitude}) · Date: ${report.generatedDate}",
            36f,
            footerY + 24f,
            paint
        )
    }

    private fun drawWrappedText(
        canvas: Canvas,
        paint: Paint,
        text: String,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        lineSpacing: Float
    ) {
        val words = text.split(" ")
        var line = ""
        var curY = startY

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, startX, curY, paint)
                line = word
                curY += lineSpacing
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, startX, curY, paint)
        }
    }
}
