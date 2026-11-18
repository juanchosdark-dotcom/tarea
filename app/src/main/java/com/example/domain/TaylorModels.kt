package com.example.domain

enum class CuttingMaterial(val displayName: String, val codeLabel: String) {
  CARBON_STEEL("Acero al Carbono", "C-Steel"),
  STAINLESS_STEEL("Acero Inoxidable", "Inox"),
  ALUMINUM("Aluminio", "Al"),
  CAST_IRON("Fundición de Hierro", "Fe-Cast"),
}

enum class CuttingTool(val displayName: String, val subtitle: String) {
  HSS("HSS", "Acero Rápido (High-Speed Steel)"),
  CARBIDE("Carburo", "Carburo Cementado (Metal Duro)"),
}

data class TaylorParameters(val n: Double, val c: Double)

data class TaylorDictionaryEntry(
  val material: CuttingMaterial,
  val tool: CuttingTool,
  val parameters: TaylorParameters,
)

object TaylorDictionary {
  private val table: Map<Pair<CuttingMaterial, CuttingTool>, TaylorParameters> =
    mapOf(
      (CuttingMaterial.CARBON_STEEL to CuttingTool.HSS) to TaylorParameters(n = 0.1, c = 80.0),
      (CuttingMaterial.CARBON_STEEL to CuttingTool.CARBIDE) to
        TaylorParameters(n = 0.25, c = 350.0),
      (CuttingMaterial.STAINLESS_STEEL to CuttingTool.HSS) to
        TaylorParameters(n = 0.08, c = 50.0),
      (CuttingMaterial.STAINLESS_STEEL to CuttingTool.CARBIDE) to
        TaylorParameters(n = 0.2, c = 200.0),
      (CuttingMaterial.ALUMINUM to CuttingTool.HSS) to TaylorParameters(n = 0.3, c = 500.0),
      (CuttingMaterial.ALUMINUM to CuttingTool.CARBIDE) to
        TaylorParameters(n = 0.5, c = 1000.0),
      (CuttingMaterial.CAST_IRON to CuttingTool.HSS) to TaylorParameters(n = 0.12, c = 60.0),
      (CuttingMaterial.CAST_IRON to CuttingTool.CARBIDE) to
        TaylorParameters(n = 0.22, c = 250.0),
    )

  fun getParameters(material: CuttingMaterial, tool: CuttingTool): TaylorParameters =
    table.getValue(material to tool)

  fun allEntries(): List<TaylorDictionaryEntry> =
    CuttingMaterial.entries.flatMap { material ->
      CuttingTool.entries.map { tool ->
        TaylorDictionaryEntry(
          material = material,
          tool = tool,
          parameters = getParameters(material, tool),
        )
      }
    }
}
