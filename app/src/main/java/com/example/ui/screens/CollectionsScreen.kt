package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipItem
import com.example.ui.components.ClipCard
import com.example.ui.components.EmptyStateView

private val FOLDER_COLORS =
  listOf(
    0xFF3B82F6L,
    0xFF8B5CF6L,
    0xFF06B6D4L,
    0xFF10B981L,
    0xFFF59E0BL,
    0xFFEF4444L,
    0xFFEC4899L,
    0xFF64748BL,
  )

@Composable
fun CollectionsScreen(
  categories: List<Category>,
  clips: List<ClipItem>,
  initialCategoryId: Long? = null,
  onCreateCategory: (String, Long) -> Unit,
  onRenameCategory: (Long, String, Long) -> Unit,
  onDeleteCategory: (Long) -> Unit,
  onOpenClipDetails: (Long) -> Unit,
  onCopyClip: (ClipItem) -> Unit,
  onTogglePin: (ClipItem) -> Unit,
  onToggleFavorite: (ClipItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  var openedCategoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var editingCategory by remember { mutableStateOf<Category?>(null) }

  if (openedCategoryId != null) {
    BackHandler { openedCategoryId = null }
  }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
      if (openedCategoryId == null) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.nav_collections),
            style = MaterialTheme.typography.titleLarge,
          )
          Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier.testTag("create_collection_button"),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("New Folder")
          }
        }

        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 156.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          items(categories, key = { it.id }) { cat ->
            var menuExpanded by remember { mutableStateOf(false) }
            ElevatedCard(
              onClick = { openedCategoryId = cat.id },
              modifier = Modifier.fillMaxWidth().testTag("collection_card_${cat.id}"),
              colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Box(
                    modifier =
                      Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(cat.colorHex).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Folder,
                      contentDescription = cat.name,
                      tint = Color(cat.colorHex),
                    )
                  }
                  Box {
                    IconButton(onClick = { menuExpanded = true }) {
                      Icon(Icons.Filled.MoreVert, contentDescription = "Collection options")
                    }
                    DropdownMenu(
                      expanded = menuExpanded,
                      onDismissRequest = { menuExpanded = false },
                    ) {
                      DropdownMenuItem(
                        text = { Text("Rename / Color") },
                        onClick = {
                          menuExpanded = false
                          editingCategory = cat
                        },
                      )
                      if (!cat.isDefault) {
                        DropdownMenuItem(
                          text = { Text(stringResource(R.string.action_delete)) },
                          onClick = {
                            menuExpanded = false
                            onDeleteCategory(cat.id)
                          },
                        )
                      }
                    }
                  }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = cat.name,
                  style = MaterialTheme.typography.titleMedium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${cat.clipCount} clips",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      } else {
        val currentCat = categories.find { it.id == openedCategoryId }
        val categoryClips = clips.filter { it.categoryId == openedCategoryId }

        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = { openedCategoryId = null }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
          }
          Box(
            modifier =
              Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(currentCat?.colorHex ?: 0xFF3B82F6))
          )
          Spacer(Modifier.size(8.dp))
          Text(
            text = currentCat?.name ?: "Collection",
            style = MaterialTheme.typography.titleLarge,
          )
        }

        if (categoryClips.isEmpty()) {
          EmptyStateView(
            title = "No clips in this folder",
            subtitle = "Assign clips here when saving or move them from All Clips.",
            showArtwork = false,
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(categoryClips, key = { it.id }) { clip ->
              ClipCard(
                clip = clip,
                category = currentCat,
                onClick = { onOpenClipDetails(clip.id) },
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
  }

  if (showCreateDialog) {
    CategoryEditorDialog(
      title = "Create Collection",
      initialName = "",
      initialColor = FOLDER_COLORS.first(),
      onDismiss = { showCreateDialog = false },
      onConfirm = { name, color ->
        showCreateDialog = false
        onCreateCategory(name, color)
      },
    )
  }

  editingCategory?.let { cat ->
    CategoryEditorDialog(
      title = "Edit Collection",
      initialName = cat.name,
      initialColor = cat.colorHex,
      onDismiss = { editingCategory = null },
      onConfirm = { name, color ->
        editingCategory = null
        onRenameCategory(cat.id, name, color)
      },
    )
  }
}

@Composable
private fun CategoryEditorDialog(
  title: String,
  initialName: String,
  initialColor: Long,
  onDismiss: () -> Unit,
  onConfirm: (String, Long) -> Unit,
) {
  var name by remember { mutableStateOf(initialName) }
  var selectedColor by remember { mutableLongStateOf(initialColor) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Folder Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("collection_name_input"),
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FOLDER_COLORS.forEach { hex ->
            Box(
              modifier =
                Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(hex))
                  .then(
                    if (selectedColor == hex) {
                      Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else Modifier
                  )
                  .clickable { selectedColor = hex }
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm(name, selectedColor) },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("save_collection_button"),
      ) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
    },
  )
}
