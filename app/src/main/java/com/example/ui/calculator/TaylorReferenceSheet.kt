package com.example.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.CuttingMaterial
import com.example.domain.CuttingTool
import com.example.domain.EngineeringFormatter
import com.example.domain.TaylorDictionary
import com.example.domain.TaylorDictionaryEntry
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaylorReferenceSheet(
  selectedMaterial: CuttingMaterial,
  selectedTool: CuttingTool,
  onSelectEntry: (CuttingMaterial, CuttingTool) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val entries = TaylorDictionary.allEntries()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp)
          .padding(bottom = 24.dp),
    ) {
      Text(
        text = stringResource(R.string.reference_sheet_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.reference_sheet_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Header row
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Material / Herramienta",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1.7f),
        )
        Text(
          text = "n",
          style =
            MaterialTheme.typography.labelMedium.copy(
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
            ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(0.6f),
        )
        Text(
          text = "C",
          style =
            MaterialTheme.typography.labelMedium.copy(
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.Bold,
            ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(0.7f),
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        items(entries) { entry ->
          val isSelected =
            entry.material == selectedMaterial && entry.tool == selectedTool
          ReferenceEntryRow(
            entry = entry,
            isSelected = isSelected,
            onClick = { onSelectEntry(entry.material, entry.tool) },
          )
        }
      }
    }
  }
}

@Composable
private fun ReferenceEntryRow(
  entry: TaylorDictionaryEntry,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    modifier =
      Modifier
        .fillMaxWidth()
        .testTag("ref_row_${entry.material.name}_${entry.tool.name}")
        .clickable(onClick = onClick),
    shape = MaterialTheme.shapes.small,
    color =
      if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
      } else {
        MaterialTheme.colorScheme.surface
      },
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1.7f)) {
        Text(
          text = entry.material.displayName,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = entry.tool.displayName,
          style = MaterialTheme.typography.labelSmall,
          color =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = EngineeringFormatter.formatPrecise(entry.parameters.n, 2),
        style =
          MaterialTheme.typography.bodyMedium.copy(
            fontFamily = MonoFontFamily,
            fontWeight = FontWeight.SemiBold,
          ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(0.6f),
      )
      Row(
        modifier = Modifier.weight(0.7f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = EngineeringFormatter.formatNumber(entry.parameters.c, 0),
          style =
            MaterialTheme.typography.bodyMedium.copy(
              fontFamily = MonoFontFamily,
              fontWeight = FontWeight.SemiBold,
            ),
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (isSelected) {
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
