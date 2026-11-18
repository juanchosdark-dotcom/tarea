package com.example.ui.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EngineeringFormatter
import com.example.domain.TaylorCalculationResult
import com.example.ui.theme.MonoFontFamily
import kotlin.math.pow

@Composable
fun TaylorCurveCard(
  result: TaylorCalculationResult,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val gridColor = MaterialTheme.colorScheme.outlineVariant
  val surfaceColor = MaterialTheme.colorScheme.surface
  val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

  val canPlot =
    result.toolLifeMinutes.isFinite() &&
      result.doubledToolLifeMinutes.isFinite() &&
      result.toolLifeMinutes > 0.0 &&
      result.velocityMPerMin > 0.0

  OutlinedCard(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.outlinedCardColors(containerColor = surfaceColor),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.result_chart_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "V·Tⁿ = ${EngineeringFormatter.formatNumber(result.parameters.c, 0)}",
          style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
          color = primaryColor,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (!canPlot) {
        Text(
          text =
            "La vida útil calculada excede el rango representable para graficar en escala lineal.",
          style = MaterialTheme.typography.bodySmall,
          color = labelColor,
        )
      } else {
        val tMin = (result.toolLifeMinutes * 0.45).coerceAtLeast(1e-6)
        val tMax = result.doubledToolLifeMinutes * 1.25
        val vMax = (result.parameters.c / tMin.pow(result.parameters.n)).coerceAtLeast(result.velocityMPerMin * 1.15)
        val vMin = (result.parameters.c / tMax.pow(result.parameters.n)) * 0.85

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "V máx: ${EngineeringFormatter.formatNumber(vMax, 1)} m/min",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
            color = labelColor,
          )
          Text(
            text = "T máx: ${EngineeringFormatter.formatNumber(tMax, 1)} min",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
            color = labelColor,
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(176.dp)
        ) {
          val padLeft = 10.dp.toPx()
          val padRight = 10.dp.toPx()
          val padTop = 14.dp.toPx()
          val padBottom = 14.dp.toPx()
          val plotW = (size.width - padLeft - padRight).coerceAtLeast(1f)
          val plotH = (size.height - padTop - padBottom).coerceAtLeast(1f)

          fun mapX(t: Double): Float {
            val frac = ((t - tMin) / (tMax - tMin)).coerceIn(0.0, 1.0)
            return padLeft + (frac * plotW).toFloat()
          }

          fun mapY(v: Double): Float {
            val span = (vMax - vMin).coerceAtLeast(1e-6)
            val frac = ((v - vMin) / span).coerceIn(0.0, 1.0)
            return padTop + ((1.0 - frac) * plotH).toFloat()
          }

          // Subtle blueprint grid (4x4)
          for (i in 0..4) {
            val y = padTop + (plotH * i / 4f)
            drawLine(
              color = gridColor,
              start = Offset(padLeft, y),
              end = Offset(padLeft + plotW, y),
              strokeWidth = 1.dp.toPx(),
            )
            val x = padLeft + (plotW * i / 4f)
            drawLine(
              color = gridColor,
              start = Offset(x, padTop),
              end = Offset(x, padTop + plotH),
              strokeWidth = 1.dp.toPx(),
            )
          }

          // Curve path & shaded area under curve
          val samples = 60
          val curvePath = Path()
          val areaPath = Path()

          for (i in 0..samples) {
            val frac = i.toDouble() / samples
            val t = tMin + frac * (tMax - tMin)
            val v = result.parameters.c / t.pow(result.parameters.n)
            val px = mapX(t)
            val py = mapY(v)
            if (i == 0) {
              curvePath.moveTo(px, py)
              areaPath.moveTo(px, padTop + plotH)
              areaPath.lineTo(px, py)
            } else {
              curvePath.lineTo(px, py)
              areaPath.lineTo(px, py)
            }
          }
          areaPath.lineTo(padLeft + plotW, padTop + plotH)
          areaPath.close()

          drawPath(
            path = areaPath,
            brush =
              Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.20f), Color.Transparent),
                startY = padTop,
                endY = padTop + plotH,
              ),
          )

          drawPath(
            path = curvePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
          )

          val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

          // Current operating point (T, V)
          val curX = mapX(result.toolLifeMinutes)
          val curY = mapY(result.velocityMPerMin)
          drawLine(
            color = primaryColor.copy(alpha = 0.55f),
            start = Offset(curX, curY),
            end = Offset(curX, padTop + plotH),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashEffect,
          )
          drawCircle(
            color = surfaceColor,
            radius = 7.dp.toPx(),
            center = Offset(curX, curY),
          )
          drawCircle(
            color = primaryColor,
            radius = 5.dp.toPx(),
            center = Offset(curX, curY),
          )

          // Doubled life point (2T, V_nueva)
          val newX = mapX(result.doubledToolLifeMinutes)
          val newY = mapY(result.newVelocityForDoubledLife)
          drawLine(
            color = secondaryColor.copy(alpha = 0.55f),
            start = Offset(newX, newY),
            end = Offset(newX, padTop + plotH),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = dashEffect,
          )
          drawCircle(
            color = surfaceColor,
            radius = 7.dp.toPx(),
            center = Offset(newX, newY),
          )
          drawCircle(
            color = secondaryColor,
            radius = 5.dp.toPx(),
            center = Offset(newX, newY),
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          ChartLegendItem(
            color = primaryColor,
            label =
              "Actual (${EngineeringFormatter.formatNumber(result.toolLifeMinutes, 2)} min, ${EngineeringFormatter.formatNumber(result.velocityMPerMin, 1)} m/min)",
            modifier = Modifier.weight(1f),
          )
          ChartLegendItem(
            color = secondaryColor,
            label =
              "2× Vida (${EngineeringFormatter.formatNumber(result.doubledToolLifeMinutes, 2)} min, ${EngineeringFormatter.formatNumber(result.newVelocityForDoubledLife, 1)} m/min)",
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun ChartLegendItem(
  color: Color,
  label: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(color)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
