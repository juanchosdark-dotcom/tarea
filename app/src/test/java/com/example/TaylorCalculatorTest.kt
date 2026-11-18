package com.example

import com.example.domain.CuttingMaterial
import com.example.domain.CuttingTool
import com.example.domain.TaylorCalculator
import com.example.domain.TaylorDictionary
import com.example.domain.ValidationError
import com.example.domain.VelocityValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class TaylorCalculatorTest {

  @Test
  fun `dictionary returns exact parameters for all 8 material and tool combinations`() {
    assertEquals(0.1, TaylorDictionary.getParameters(CuttingMaterial.CARBON_STEEL, CuttingTool.HSS).n, 1e-9)
    assertEquals(80.0, TaylorDictionary.getParameters(CuttingMaterial.CARBON_STEEL, CuttingTool.HSS).c, 1e-9)

    assertEquals(0.25, TaylorDictionary.getParameters(CuttingMaterial.CARBON_STEEL, CuttingTool.CARBIDE).n, 1e-9)
    assertEquals(350.0, TaylorDictionary.getParameters(CuttingMaterial.CARBON_STEEL, CuttingTool.CARBIDE).c, 1e-9)

    assertEquals(0.08, TaylorDictionary.getParameters(CuttingMaterial.STAINLESS_STEEL, CuttingTool.HSS).n, 1e-9)
    assertEquals(50.0, TaylorDictionary.getParameters(CuttingMaterial.STAINLESS_STEEL, CuttingTool.HSS).c, 1e-9)

    assertEquals(0.2, TaylorDictionary.getParameters(CuttingMaterial.STAINLESS_STEEL, CuttingTool.CARBIDE).n, 1e-9)
    assertEquals(200.0, TaylorDictionary.getParameters(CuttingMaterial.STAINLESS_STEEL, CuttingTool.CARBIDE).c, 1e-9)

    assertEquals(0.3, TaylorDictionary.getParameters(CuttingMaterial.ALUMINUM, CuttingTool.HSS).n, 1e-9)
    assertEquals(500.0, TaylorDictionary.getParameters(CuttingMaterial.ALUMINUM, CuttingTool.HSS).c, 1e-9)

    assertEquals(0.5, TaylorDictionary.getParameters(CuttingMaterial.ALUMINUM, CuttingTool.CARBIDE).n, 1e-9)
    assertEquals(1000.0, TaylorDictionary.getParameters(CuttingMaterial.ALUMINUM, CuttingTool.CARBIDE).c, 1e-9)

    assertEquals(0.12, TaylorDictionary.getParameters(CuttingMaterial.CAST_IRON, CuttingTool.HSS).n, 1e-9)
    assertEquals(60.0, TaylorDictionary.getParameters(CuttingMaterial.CAST_IRON, CuttingTool.HSS).c, 1e-9)

    assertEquals(0.22, TaylorDictionary.getParameters(CuttingMaterial.CAST_IRON, CuttingTool.CARBIDE).n, 1e-9)
    assertEquals(250.0, TaylorDictionary.getParameters(CuttingMaterial.CAST_IRON, CuttingTool.CARBIDE).c, 1e-9)
  }

  @Test
  fun `velocity validation rejects empty, zero, negative, and invalid inputs`() {
    assertEquals(
      VelocityValidation.Invalid(ValidationError.EMPTY),
      TaylorCalculator.parseAndValidateVelocity("   "),
    )
    assertEquals(
      VelocityValidation.Invalid(ValidationError.NON_POSITIVE),
      TaylorCalculator.parseAndValidateVelocity("0"),
    )
    assertEquals(
      VelocityValidation.Invalid(ValidationError.NON_POSITIVE),
      TaylorCalculator.parseAndValidateVelocity("-25.5"),
    )
    assertEquals(
      VelocityValidation.Invalid(ValidationError.NOT_A_NUMBER),
      TaylorCalculator.parseAndValidateVelocity("abc"),
    )
    val validComma = TaylorCalculator.parseAndValidateVelocity("120,5")
    assertTrue(validComma is VelocityValidation.Valid)
    assertEquals(120.5, (validComma as VelocityValidation.Valid).velocity, 1e-9)
  }

  @Test
  fun `taylor calculation matches formulas for tool life and doubled life speed`() {
    // Acero al Carbono + HSS (n=0.1, C=80), V = 40 m/min
    // T = (80 / 40)^(1 / 0.1) = 2^10 = 1024 min
    // V_nueva = 40 * (0.5^0.1)
    val result =
      TaylorCalculator.calculate(
        material = CuttingMaterial.CARBON_STEEL,
        tool = CuttingTool.HSS,
        velocityMPerMin = 40.0,
      )
    assertEquals(1024.0, result.toolLifeMinutes, 1e-6)
    assertEquals(40.0 * 0.5.pow(0.1), result.newVelocityForDoubledLife, 1e-9)
    assertEquals(2048.0, result.doubledToolLifeMinutes, 1e-6)
  }
}
