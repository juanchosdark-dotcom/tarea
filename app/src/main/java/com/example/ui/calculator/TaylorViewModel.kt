package com.example.ui.calculator

import androidx.lifecycle.ViewModel
import com.example.domain.CuttingMaterial
import com.example.domain.CuttingTool
import com.example.domain.TaylorCalculationResult
import com.example.domain.TaylorCalculator
import com.example.domain.TaylorDictionary
import com.example.domain.TaylorParameters
import com.example.domain.ValidationError
import com.example.domain.VelocityValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TaylorUiState(
  val selectedMaterial: CuttingMaterial = CuttingMaterial.CARBON_STEEL,
  val selectedTool: CuttingTool = CuttingTool.HSS,
  val velocityInput: String = "",
  val velocityError: ValidationError? = null,
  val activeParameters: TaylorParameters =
    TaylorDictionary.getParameters(CuttingMaterial.CARBON_STEEL, CuttingTool.HSS),
  val calculationResult: TaylorCalculationResult? = null,
  val isReferenceSheetVisible: Boolean = false,
)

class TaylorViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(TaylorUiState())
  val uiState: StateFlow<TaylorUiState> = _uiState.asStateFlow()

  fun onMaterialSelected(material: CuttingMaterial) {
    _uiState.update { current ->
      current.copy(
        selectedMaterial = material,
        activeParameters = TaylorDictionary.getParameters(material, current.selectedTool),
        calculationResult = null,
      )
    }
  }

  fun onToolSelected(tool: CuttingTool) {
    _uiState.update { current ->
      current.copy(
        selectedTool = tool,
        activeParameters = TaylorDictionary.getParameters(current.selectedMaterial, tool),
        calculationResult = null,
      )
    }
  }

  fun onPresetSelected(material: CuttingMaterial, tool: CuttingTool) {
    _uiState.update { current ->
      current.copy(
        selectedMaterial = material,
        selectedTool = tool,
        activeParameters = TaylorDictionary.getParameters(material, tool),
        calculationResult = null,
        isReferenceSheetVisible = false,
      )
    }
  }

  fun onVelocityChanged(input: String) {
    // Allow digits, decimal separators (dot or comma), and minus sign so validation can catch <= 0
    val filtered = input.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
    _uiState.update { current ->
      current.copy(
        velocityInput = filtered,
        velocityError = null,
        calculationResult = null,
      )
    }
  }

  fun onCalculateClick() {
    val current = _uiState.value
    when (val validation = TaylorCalculator.parseAndValidateVelocity(current.velocityInput)) {
      is VelocityValidation.Invalid -> {
        _uiState.update {
          it.copy(
            velocityError = validation.reason,
            calculationResult = null,
          )
        }
      }
      is VelocityValidation.Valid -> {
        val result =
          TaylorCalculator.calculate(
            material = current.selectedMaterial,
            tool = current.selectedTool,
            velocityMPerMin = validation.velocity,
          )
        _uiState.update {
          it.copy(
            velocityError = null,
            calculationResult = result,
          )
        }
      }
    }
  }

  fun onToggleReferenceSheet(visible: Boolean) {
    _uiState.update { it.copy(isReferenceSheetVisible = visible) }
  }
}
