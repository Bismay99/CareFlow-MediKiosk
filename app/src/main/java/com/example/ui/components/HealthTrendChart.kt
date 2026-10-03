package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthMetricPoint
import com.example.data.model.HealthMetricType

@Composable
fun HealthTrendChart(
    points: List<HealthMetricPoint>,
    metricType: HealthMetricType,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "No biometric observations recorded in this window.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
        return
    }

    val values = points.map { it.value }
    val rawMin = values.minOrNull() ?: 0f
    val rawMax = values.maxOrNull() ?: 100f
    val paddingY = ((rawMax - rawMin) * 0.15f).coerceAtLeast(2f)
    val minY = rawMin - paddingY
    val maxY = rawMax + paddingY
    val rangeY = (maxY - minY).coerceAtLeast(1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Value Range Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Peak: ${String.format("%.1f", rawMax)} ${metricType.unit}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Text(
                text = "Avg: ${String.format("%.1f", values.average())} ${metricType.unit}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = lineColor)
            )
            Text(
                text = "Low: ${String.format("%.1f", rawMin)} ${metricType.unit}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Canvas Area
        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height

            // 1. Draw horizontal grid guidelines
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            if (points.size == 1) {
                val pt = points.first()
                val y = height - ((pt.value - minY) / rangeY) * height
                drawCircle(
                    color = lineColor,
                    radius = 6.dp.toPx(),
                    center = Offset(width / 2f, y)
                )
                return@Canvas
            }

            val stepX = width / (points.size - 1)

            // Compute coordinates
            val path = Path()
            val fillPath = Path()

            val coordinates = points.mapIndexed { index, pt ->
                val x = index * stepX
                val y = height - ((pt.value - minY) / rangeY) * height
                Offset(x, y)
            }

            path.moveTo(coordinates.first().x, coordinates.first().y)
            fillPath.moveTo(coordinates.first().x, height)
            fillPath.lineTo(coordinates.first().x, coordinates.first().y)

            for (i in 1 until coordinates.size) {
                val prev = coordinates[i - 1]
                val curr = coordinates[i]
                val cpx1 = (prev.x + curr.x) / 2f
                val cpy1 = prev.y
                val cpx2 = (prev.x + curr.x) / 2f
                val cpy2 = curr.y

                path.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
                fillPath.cubicTo(cpx1, cpy1, cpx2, cpy2, curr.x, curr.y)
            }

            fillPath.lineTo(coordinates.last().x, height)
            fillPath.close()

            // Draw Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Line Stroke
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw data points
            coordinates.forEach { coord ->
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = coord
                )
                drawCircle(
                    color = lineColor,
                    radius = 2.5.dp.toPx(),
                    center = coord
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = points.firstOrNull()?.formattedDate ?: ""
            val midDate = points.getOrNull(points.size / 2)?.formattedDate ?: ""
            val lastDate = points.lastOrNull()?.formattedDate ?: ""

            Text(
                text = firstDate,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
            )
            if (points.size > 2) {
                Text(
                    text = midDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }
            Text(
                text = lastDate,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
    }
}
