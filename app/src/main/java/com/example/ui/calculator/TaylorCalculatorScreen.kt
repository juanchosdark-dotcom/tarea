package com.example.ui.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.CuttingMaterial
import com.example.domain.CuttingTool
import com.example.domain.EngineeringFormatter
import com.example.domain.TaylorParameters
import com.example.domain.ValidationError
import com.example.ui.theme.MonoFontFamily

@Composable
fun TaylorCalculatorRoute(
  viewModel: TaylorViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  TaylorCalculatorScreen(
    uiState = uiState,
    onMaterialSelected = viewModel::onMaterialSelected,
    onToolSelected = viewModel::onToolSelected,
    onVelocityChanged = viewModel::onVelocityChanged,
    onCalculateClick = viewModel::onCalculateClick,
    onOpenReferenceSheet = { viewModel.onToggleReferenceSheet(true) },
    onDismissReferenceSheet = { viewModel.onToggleReferenceSheet(false) },
    onPresetSelected = viewModel::onPresetSelected,
  )
}

@Composable
fun TaylorCalculatorScreen(
  uiState: TaylorUiState,
  onMaterialSelected: (CuttingMaterial) -> Unit,
  onToolSelected: (CuttingTool) -> Unit,
  onVelocityChanged: (String) -> Unit,
  onCalculateClick: () -> Unit,
  onOpenReferenceSheet: () -> Unit,
  onDismissReferenceSheet: () -> Unit,
  onPresetSelected: (CuttingMaterial, CuttingTool) -> Unit,
) {
  val keyboardController = LocalSoftwareKeyboardController.current

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    BoxWithConstraints(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      val isWideLayout = maxWidth >= 720.dp
      val scrollState = rememberScrollState()

      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .widthIn(max = 1040.dp)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        TaylorHeroBanner(onOpenReferenceSheet = onOpenReferenceSheet)

        if (isWideLayout) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
          ) {
            InputConfigurationCard(
              uiState = uiState,
              onMaterialSelected = onMaterialSelected,
              onToolSelected = onToolSelected,
              onVelocityChanged = onVelocityChanged,
              onCalculateClick = {
                keyboardController?.hide()
                onCalculateClick()
              },
              modifier = Modifier.weight(0.92f),
            )

            AnimatedContent(
              targetState = uiState.calculationResult,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "results_transition_wide",
              modifier = Modifier.weight(1.08f),
            ) { result ->
              TaylorResultsSection(result = result)
            }
          }
        } else {
          InputConfigurationCard(
            uiState = uiState,
            onMaterialSelected = onMaterialSelected,
            onToolSelected = onToolSelected,
            onVelocityChanged = onVelocityChanged,
            onCalculateClick = {
              keyboardController?.hide()
              onCalculateClick()
            },
          )

          AnimatedContent(
            targetState = uiState.calculationResult,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "results_transition_compact",
          ) { result ->
            TaylorResultsSection(result = result)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }

  if (uiState.isReferenceSheetVisible) {
    TaylorReferenceSheet(
      selectedMaterial = uiState.selectedMaterial,
      selectedTool = uiState.selectedTool,
      onSelectEntry = onPresetSelected,
      onDismiss = onDismissReferenceSheet,
    )
  }
}

@Composable
private fun TaylorHeroBanner(
  onOpenReferenceSheet: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A23)),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(128.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_taylor_banner),
        contentDescription = stringResource(R.string.cd_banner_image),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )

      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors =
                  listOf(
                    Color(0xF011161F),
                    Color(0xB811161F),
                    Color(0x5511161F),
                  )
              )
            )
      )

      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(
              text = "V · Tⁿ = C",
              style =
                MaterialTheme.typography.labelMedium.copy(
                  fontFamily = MonoFontFamily,
                  fontWeight = FontWeight.Bold,
                ),
              color = Color(0xFFFFB284),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }

          Text(
            text = stringResource(R.string.header_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
          )
          Text(
            text = stringResource(R.string.header_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
          )
        }

        FilledTonalIconButton(
          onClick = onOpenReferenceSheet,
          modifier = Modifier.size(48.dp).testTag("reference_table_button"),
          colors =
            IconButtonDefaults.filledTonalIconButtonColors(
              containerColor = Color(0xCC243042),
              contentColor = Color.White,
            ),
        ) {
          Icon(
            imageVector = Icons.Rounded.TableChart,
            contentDescription = stringResource(R.string.cd_reference_table),
          )
        }
      }
    }
  }
}

@Composable
private fun InputConfigurationCard(
  uiState: TaylorUiState,
  onMaterialSelected: (CuttingMaterial) -> Unit,
  onToolSelected: (CuttingTool) -> Unit,
  onVelocityChanged: (String) -> Unit,
  onCalculateClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val errorMessage =
    when (uiState.velocityError) {
      ValidationError.EMPTY -> stringResource(R.string.error_velocity_empty)
      ValidationError.NOT_A_NUMBER -> stringResource(R.string.error_velocity_invalid)
      ValidationError.NON_POSITIVE -> stringResource(R.string.error_velocity_non_positive)
      null -> null
    }

  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = stringResource(R.string.section_inputs_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      MaterialDropdown(
        selectedMaterial = uiState.selectedMaterial,
        onMaterialSelected = onMaterialSelected,
      )

      ToolDropdown(
        selectedTool = uiState.selectedTool,
        onToolSelected = onToolSelected,
      )

      ActiveParametersStrip(parameters = uiState.activeParameters)

      OutlinedTextField(
        value = uiState.velocityInput,
        onValueChange = onVelocityChanged,
        label = { Text(stringResource(R.string.label_velocity)) },
        placeholder = { Text(stringResource(R.string.hint_velocity)) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Rounded.Speed,
            contentDescription = null,
          )
        },
        trailingIcon = {
          if (errorMessage != null) {
            Icon(
              imageVector = Icons.Rounded.ErrorOutline,
              contentDescription = stringResource(R.string.cd_error_icon),
              tint = MaterialTheme.colorScheme.error,
            )
          } else {
            Text(
              text = stringResource(R.string.unit_m_min),
              style =
                MaterialTheme.typography.labelLarge.copy(
                  fontFamily = MonoFontFamily,
                  fontWeight = FontWeight.Medium,
                ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(end = 12.dp),
            )
          }
        },
        isError = errorMessage != null,
        supportingText =
          errorMessage?.let { msg ->
            {
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("velocity_error_text"),
              )
            }
          },
        singleLine = true,
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
          ),
        keyboardActions = KeyboardActions(onDone = { onCalculateClick() }),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().testTag("velocity_input"),
      )

      Button(
        onClick = onCalculateClick,
        shape = MaterialTheme.shapes.small,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        modifier =
          Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("calculate_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.Calculate,
          contentDescription = stringResource(R.string.cd_calculate_icon),
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
          text = stringResource(R.string.action_calculate),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialDropdown(
  selectedMaterial: CuttingMaterial,
  onMaterialSelected: (CuttingMaterial) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
  ) {
    OutlinedTextField(
      value = selectedMaterial.displayName,
      onValueChange = {},
      readOnly = true,
      label = { Text(stringResource(R.string.label_material)) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.Layers,
          contentDescription = null,
        )
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      shape = MaterialTheme.shapes.small,
      modifier =
        Modifier
          .menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("material_dropdown"),
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      CuttingMaterial.entries.forEach { material ->
        DropdownMenuItem(
          text = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = material.displayName,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = material.codeLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          onClick = {
            onMaterialSelected(material)
            expanded = false
          },
          modifier = Modifier.testTag("material_option_${material.name}"),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolDropdown(
  selectedTool: CuttingTool,
  onToolSelected: (CuttingTool) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
  ) {
    OutlinedTextField(
      value = "${selectedTool.displayName} — ${selectedTool.subtitle}",
      onValueChange = {},
      readOnly = true,
      label = { Text(stringResource(R.string.label_tool)) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.Build,
          contentDescription = null,
        )
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      shape = MaterialTheme.shapes.small,
      modifier =
        Modifier
          .menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("tool_dropdown"),
    )

    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      CuttingTool.entries.forEach { tool ->
        DropdownMenuItem(
          text = {
            Column {
              Text(
                text = tool.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
              )
              Text(
                text = tool.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          onClick = {
            onToolSelected(tool)
            expanded = false
          },
          modifier = Modifier.testTag("tool_option_${tool.name}"),
        )
      }
    }
  }
}

@Composable
private fun ActiveParametersStrip(parameters: TaylorParameters) {
  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.small)
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outlineVariant,
          shape = MaterialTheme.shapes.small,
        )
        .testTag("active_parameters_strip"),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    shape = MaterialTheme.shapes.small,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Constantes de Taylor",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ParameterBadge(
          symbol = "n",
          value = EngineeringFormatter.formatPrecise(parameters.n, 2),
        )
        ParameterBadge(
          symbol = "C",
          value = EngineeringFormatter.formatNumber(parameters.c, 0),
        )
      }
    }
  }
}

@Composable
private fun ParameterBadge(symbol: String, value: String) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShapeBadge,
    tonalElevation = 1.dp,
  ) {
    Text(
      text = "$symbol = $value",
      style =
        MaterialTheme.typography.labelMedium.copy(
          fontFamily = MonoFontFamily,
          fontWeight = FontWeight.Bold,
        ),
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

private val RoundedCornerShapeBadge = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
