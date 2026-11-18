package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EngineeringFormatter
import com.example.domain.TaylorCalculationResult
import com.example.ui.theme.MonoFontFamily

@Composable
fun TaylorResultsSection(
  result: TaylorCalculationResult?,
  modifier: Modifier = Modifier,
) {
  if (result == null) {
    EmptyResultsStateCard(modifier = modifier)
    return
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Primary KPI cards row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      MetricHighlightCard(
        title = stringResource(R.string.result_tool_life_title),
        value = EngineeringFormatter.formatNumber(result.toolLifeMinutes, 2),
        unit = stringResource(R.string.unit_min),
        subtitle =
          EngineeringFormatter.formatDurationHint(result.toolLifeMinutes)
            ?: "${result.material.codeLabel} · ${result.tool.displayName}",
        icon = Icons.Rounded.Timer,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        accentColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.weight(1f).testTag("result_tool_life_card"),
      )

      MetricHighlightCard(
        title = stringResource(R.string.result_new_speed_title),
        value = EngineeringFormatter.formatNumber(result.newVelocityForDoubledLife, 2),
        unit = stringResource(R.string.unit_m_min),
        subtitle =
          "−${EngineeringFormatter.formatNumber(result.speedReductionPercent, 1)}% vel. → ${EngineeringFormatter.formatNumber(result.doubledToolLifeMinutes, 2)} min",
        icon = Icons.Rounded.Speed,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        accentColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.weight(1f).testTag("result_new_speed_card"),
      )
    }

    // Taylor V-T Curve visualization
    TaylorCurveCard(result = result)

    // Step-by-step calculation breakdown
    CalculationBreakdownCard(result = result)
  }
}

@Composable
private fun MetricHighlightCard(
  title: String,
  value: String,
  unit: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  accentColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = containerColor),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(accentColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = contentColor.copy(alpha = 0.85f),
          maxLines = 2,
        )
      }

      Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = value,
          style =
            MaterialTheme.typography.headlineMedium.copy(
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
            ),
          color = contentColor,
        )
        Text(
          text = unit,
          style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
          color = accentColor,
          modifier = Modifier.padding(bottom = 3.dp),
        )
      }

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = contentColor.copy(alpha = 0.8f),
      )
    }
  }
}

@Composable
private fun CalculationBreakdownCard(
  result: TaylorCalculationResult,
  modifier: Modifier = Modifier,
) {
  val nStr = EngineeringFormatter.formatPrecise(result.parameters.n, 4)
  val cStr = EngineeringFormatter.formatNumber(result.parameters.c, 2)
  val vStr = EngineeringFormatter.formatNumber(result.velocityMPerMin, 2)
  val cOverVStr = EngineeringFormatter.formatPrecise(result.cOverV, 4)
  val expStr = EngineeringFormatter.formatPrecise(result.exponentOneOverN, 4)
  val tStr = EngineeringFormatter.formatNumber(result.toolLifeMinutes, 2)
  val halfPowNStr = EngineeringFormatter.formatPrecise(result.halfPowerN, 4)
  val vNewStr = EngineeringFormatter.formatNumber(result.newVelocityForDoubledLife, 2)
  val tDoubledStr = EngineeringFormatter.formatNumber(result.doubledToolLifeMinutes, 2)

  OutlinedCard(
    modifier = modifier.fillMaxWidth().testTag("breakdown_card"),
    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Functions,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = stringResource(R.string.result_breakdown_title),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      // Step 1: Dictionary parameters
      StepBlock(
        stepNumber = "1",
        title = "Parámetros del diccionario",
        accentColor = MaterialTheme.colorScheme.tertiary,
        lines =
          listOf(
            "Material: ${result.material.displayName}",
            "Herramienta: ${result.tool.displayName}",
            "n = $nStr   |   C = $cStr   |   V = $vStr m/min",
          ),
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // Step 2: Tool life T
      StepBlock(
        stepNumber = "2",
        title = "Cálculo de vida útil (T)",
        accentColor = MaterialTheme.colorScheme.primary,
        lines =
          listOf(
            "T = (C / V)^(1 / n)",
            "T = ($cStr / $vStr)^(1 / $nStr)",
            "T = ($cOverVStr)^($expStr)",
            "T = $tStr min",
          ),
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      // Step 3: Doubled life velocity V_nueva
      StepBlock(
        stepNumber = "3",
        title = "Velocidad para duplicar la vida útil (2T = $tDoubledStr min)",
        accentColor = MaterialTheme.colorScheme.secondary,
        lines =
          listOf(
            "V_nueva = V × (0.5 ^ n)",
            "V_nueva = $vStr × (0.5 ^ $nStr)",
            "V_nueva = $vStr × $halfPowNStr",
            "V_nueva = $vNewStr m/min",
          ),
      )
    }
  }
}

@Composable
private fun StepBlock(
  stepNumber: String,
  title: String,
  accentColor: Color,
  lines: List<String>,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stepNumber,
          style =
            MaterialTheme.typography.labelSmall.copy(
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
            ),
          color = accentColor,
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }

    Surface(
      modifier =
        Modifier
          .fillMaxWidth()
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = MaterialTheme.shapes.small,
          ),
      shape = MaterialTheme.shapes.small,
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        lines.forEachIndexed { index, line ->
          val isFinalLine = index == lines.lastIndex
          Text(
            text = line,
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontFamily = MonoFontFamily,
                fontWeight = if (isFinalLine) FontWeight.Bold else FontWeight.Normal,
              ),
            color = if (isFinalLine) accentColor else MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyResultsStateCard(modifier: Modifier = Modifier) {
  OutlinedCard(
    modifier = modifier.fillMaxWidth().testTag("empty_results_card"),
    colors =
      CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
      ),
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier =
          Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.AutoGraph,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(28.dp),
        )
      }
      Text(
        text = stringResource(R.string.empty_state_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = stringResource(R.string.empty_state_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
