package com.example.domain

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.pow

data class TaylorCalculationResult(
  val material: CuttingMaterial,
  val tool: CuttingTool,
  val parameters: TaylorParameters,
  val velocityMPerMin: Double,
  val cOverV: Double,
  val exponentOneOverN: Double,
  val toolLifeMinutes: Double,
  val doubledToolLifeMinutes: Double,
  val halfPowerN: Double,
  val newVelocityForDoubledLife: Double,
  val speedReductionPercent: Double,
)

sealed interface VelocityValidation {
  data class Valid(val velocity: Double) : VelocityValidation

  data class Invalid(val reason: ValidationError) : VelocityValidation
}

enum class ValidationError {
  EMPTY,
  NOT_A_NUMBER,
  NON_POSITIVE,
}

object TaylorCalculator {

  fun parseAndValidateVelocity(rawInput: String): VelocityValidation {
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) {
      return VelocityValidation.Invalid(ValidationError.EMPTY)
    }
    val normalized = trimmed.replace(',', '.')
    val parsed = normalized.toDoubleOrNull()
    if (parsed == null || parsed.isNaN() || parsed.isInfinite()) {
      return VelocityValidation.Invalid(ValidationError.NOT_A_NUMBER)
    }
    if (parsed <= 0.0) {
      return VelocityValidation.Invalid(ValidationError.NON_POSITIVE)
    }
    return VelocityValidation.Valid(parsed)
  }

  fun calculate(
    material: CuttingMaterial,
    tool: CuttingTool,
    velocityMPerMin: Double,
  ): TaylorCalculationResult {
    require(velocityMPerMin > 0.0) { "La velocidad de corte debe ser mayor a 0." }
    val params = TaylorDictionary.getParameters(material, tool)
    val cOverV = params.c / velocityMPerMin
    val exponentOneOverN = 1.0 / params.n
    val toolLifeMinutes = cOverV.pow(exponentOneOverN)
    val doubledToolLifeMinutes = toolLifeMinutes * 2.0
    val halfPowerN = 0.5.pow(params.n)
    val newVelocityForDoubledLife = velocityMPerMin * halfPowerN
    val speedReductionPercent = (1.0 - halfPowerN) * 100.0

    return TaylorCalculationResult(
      material = material,
      tool = tool,
      parameters = params,
      velocityMPerMin = velocityMPerMin,
      cOverV = cOverV,
      exponentOneOverN = exponentOneOverN,
      toolLifeMinutes = toolLifeMinutes,
      doubledToolLifeMinutes = doubledToolLifeMinutes,
      halfPowerN = halfPowerN,
      newVelocityForDoubledLife = newVelocityForDoubledLife,
      speedReductionPercent = speedReductionPercent,
    )
  }
}

object EngineeringFormatter {
  private val symbols = DecimalFormatSymbols(Locale.US)

  fun formatNumber(value: Double, maxDecimals: Int = 2): String {
    if (value.isInfinite()) return "∞"
    if (value.isNaN()) return "NaN"
    if (value >= 1_000_000.0) {
      return String.format(Locale.US, "%.3e", value)
    }
    val pattern =
      if (maxDecimals <= 0) "#,##0" else "#,##0." + "#".repeat(maxDecimals)
    return DecimalFormat(pattern, symbols).format(value)
  }

  fun formatPrecise(value: Double, decimals: Int = 4): String {
    if (value.isInfinite()) return "∞"
    if (value.isNaN()) return "NaN"
    if (value >= 1_000_000.0) {
      return String.format(Locale.US, "%.3e", value)
    }
    val pattern = "0." + "#".repeat(decimals)
    return DecimalFormat(pattern, symbols).format(value)
  }

  fun formatDurationHint(minutes: Double): String? {
    if (!minutes.isFinite() || minutes < 60.0) return null
    val hours = minutes / 60.0
    return if (hours < 24.0) {
      "≈ ${formatNumber(hours, 2)} horas"
    } else {
      val days = hours / 24.0
      "≈ ${formatNumber(hours, 1)} h (${formatNumber(days, 1)} días)"
    }
  }
}
