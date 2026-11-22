package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipItem
import com.example.ui.components.ClipCard
import com.example.ui.components.EmptyStateView

@Composable
fun AllClipsScreen(
  clips: List<ClipItem>,
  categories: List<Category>,
  selectedClipIds: Set<Long>,
  onToggleSelection: (Long) -> Unit,
  onSelectAll: (List<Long>) -> Unit,
  onClearSelection: () -> Unit,
  onBulkDelete: (List<Long>) -> Unit,
  onBulkMoveCategory: (Long) -> Unit,
  onOpenClipDetails: (Long) -> Unit,
  onCopyClip: (ClipItem) -> Unit,
  onTogglePin: (ClipItem) -> Unit,
  onToggleFavorite: (ClipItem) -> Unit,
  onAddNewClip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var filterCategoryId by remember { mutableStateOf<Long?>(null) }
  var showMoveMenu by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val categoryMap = categories.associateBy { it.id }
  val filtered =
    remember(clips, filterCategoryId) {
      if (filterCategoryId == null) clips else clips.filter { it.categoryId == filterCategoryId }
    }
  val inSelectionMode = selectedClipIds.isNotEmpty()

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
      if (inSelectionMode) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onClearSelection, modifier = Modifier.testTag("clear_selection_btn")) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_clear_selection))
              }
              Text(
                text = "${selectedClipIds.size} selected",
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Row {
              IconButton(
                onClick = { onSelectAll(filtered.map { it.id }) },
                modifier = Modifier.testTag("select_all_btn"),
              ) {
                Icon(Icons.Filled.SelectAll, contentDescription = stringResource(R.string.action_select_all))
              }
              IconButton(
                onClick = { showMoveMenu = true },
                modifier = Modifier.testTag("bulk_move_btn"),
              ) {
                Icon(Icons.Filled.DriveFileMove, contentDescription = "Move to collection")
                DropdownMenu(expanded = showMoveMenu, onDismissRequest = { showMoveMenu = false }) {
                  categories.forEach { cat ->
                    DropdownMenuItem(
                      text = { Text(cat.name) },
                      onClick = {
                        showMoveMenu = false
                        onBulkMoveCategory(cat.id)
                      },
                    )
                  }
                }
              }
              IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.testTag("bulk_delete_btn"),
              ) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
              }
            }
          }
        }
      } else {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = filterCategoryId == null,
            onClick = { filterCategoryId = null },
            label = { Text(stringResource(R.string.filter_all)) },
          )
          categories.forEach { cat ->
            FilterChip(
              selected = filterCategoryId == cat.id,
              onClick = { filterCategoryId = if (filterCategoryId == cat.id) null else cat.id },
              label = { Text("${cat.name} (${cat.clipCount})") },
            )
          }
        }
      }

      if (filtered.isEmpty()) {
        EmptyStateView(
          title = stringResource(R.string.empty_clips_title),
          subtitle = stringResource(R.string.empty_clips_subtitle),
          actionLabel = stringResource(R.string.action_add_clip),
          onAction = onAddNewClip,
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(filtered, key = { it.id }) { clip ->
            ClipCard(
              clip = clip,
              category = categoryMap[clip.categoryId],
              isSelected = selectedClipIds.contains(clip.id),
              inSelectionMode = inSelectionMode,
              onClick = {
                if (inSelectionMode) onToggleSelection(clip.id) else onOpenClipDetails(clip.id)
              },
              onLongClick = { onToggleSelection(clip.id) },
              onCopyClick = { onCopyClip(clip) },
              onPinToggle = { onTogglePin(clip) },
              onFavoriteToggle = { onToggleFavorite(clip) },
            )
          }
          item { Spacer(modifier = Modifier.height(72.dp)) }
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text(stringResource(R.string.action_delete)) },
      text = { Text("Move ${selectedClipIds.size} selected item(s) to Trash?") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirm = false
            onBulkDelete(selectedClipIds.toList())
          }
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }
}
