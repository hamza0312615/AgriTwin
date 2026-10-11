package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.AgriPrimaryFresh
import com.example.ui.theme.SkyBlue

/**
 * High-precision vector sparkline chart for light "sunlit agriculture" theme.
 */
@Composable
fun SparklineChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = AgriPrimaryFresh,
    fillAlpha: Float = 0.18f,
    showPoints: Boolean = true
) {
    if (values.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val minVal = (values.minOrNull() ?: 0).coerceAtLeast(0)
        val maxVal = (values.maxOrNull() ?: 100).coerceAtLeast(minVal + 10)
        val range = (maxVal - minVal).toFloat().coerceAtLeast(1f)

        val points = values.mapIndexed { index, v ->
            val x = if (values.size > 1) {
                index * (width / (values.size - 1))
            } else {
                width / 2f
            }
            val normY = (v - minVal) / range
            val y = height - (normY * (height * 0.72f) + (height * 0.14f))
            Offset(x, y)
        }

        // 1. Draw gradient fill underneath
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = fillAlpha), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )
        }

        // 2. Draw smooth stroke path
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cx = (prev.x + curr.x) / 2f
                cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
            }
        }
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // 3. Draw key marker circles
        if (showPoints) {
            points.forEachIndexed { idx, pt ->
                val isLast = idx == points.size - 1
                drawCircle(
                    color = if (isLast) SkyBlue else lineColor,
                    radius = if (isLast) 5.5f else 3.5f,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isLast) 3f else 1.8f,
                    center = pt
                )
            }
        }
    }
}
