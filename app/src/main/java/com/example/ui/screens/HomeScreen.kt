package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipItem
import com.example.ui.components.ClipCard
import com.example.ui.components.EmptyStateView

@Composable
fun HomeScreen(
  clips: List<ClipItem>,
  categories: List<Category>,
  detectedClipboardText: String?,
  onCaptureClipboardNow: () -> Unit,
  onSaveDetectedClipboard: () -> Unit,
  onDismissDetectedClipboard: () -> Unit,
  onAddNewClip: () -> Unit,
  onOpenClipDetails: (Long) -> Unit,
  onCopyClip: (ClipItem) -> Unit,
  onTogglePin: (ClipItem) -> Unit,
  onToggleFavorite: (ClipItem) -> Unit,
  onOpenFavorites: () -> Unit,
  onOpenIntegrations: () -> Unit,
  onOpenTrash: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenBackupSync: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val categoryMap = categories.associateBy { it.id }
  val pinnedClips = clips.filter { it.isPinned }
  val recentClips = clips.take(25)

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Quick Actions Strip
      item {
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          AssistChip(
            onClick = onCaptureClipboardNow,
            modifier = Modifier.testTag("home_capture_chip"),
            leadingIcon = { Icon(Icons.Filled.ContentPaste, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.action_paste_from_clipboard)) },
          )
          AssistChip(
            onClick = onOpenFavorites,
            modifier = Modifier.testTag("home_favorites_chip"),
            leadingIcon = { Icon(Icons.Filled.Star, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.nav_favorites)) },
          )
          AssistChip(
            onClick = onOpenIntegrations,
            modifier = Modifier.testTag("home_integrations_chip"),
            leadingIcon = { Icon(Icons.Filled.Hub, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.nav_integrations)) },
          )
          AssistChip(
            onClick = onOpenBackupSync,
            modifier = Modifier.testTag("home_sync_chip"),
            leadingIcon = { Icon(Icons.Filled.CloudSync, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.nav_backup_sync)) },
          )
          AssistChip(
            onClick = onOpenTrash,
            modifier = Modifier.testTag("home_trash_chip"),
            leadingIcon = { Icon(Icons.Filled.DeleteOutline, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.nav_trash)) },
          )
          AssistChip(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("home_settings_chip"),
            leadingIcon = { Icon(Icons.Filled.Settings, null, Modifier.size(18.dp)) },
            label = { Text(stringResource(R.string.nav_settings)) },
          )
        }
      }

      // Foreground Detected Clipboard Banner
      item {
        AnimatedVisibility(visible = !detectedClipboardText.isNullOrBlank()) {
          ElevatedCard(
            colors =
              CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
              ),
            modifier = Modifier.fillMaxWidth().testTag("detected_clipboard_card"),
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = stringResource(R.string.home_clipboard_detected),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = detectedClipboardText.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = onSaveDetectedClipboard,
                  modifier = Modifier.testTag("save_detected_button"),
                ) {
                  Text(stringResource(R.string.home_save_detected))
                }
                TextButton(
                  onClick = onDismissDetectedClipboard,
                  modifier = Modifier.testTag("dismiss_detected_button"),
                ) {
                  Text(stringResource(R.string.action_cancel))
                }
              }
            }
          }
        }
      }

      // Pinned Carousel
      if (pinnedClips.isNotEmpty()) {
        item {
          Text(
            text = stringResource(R.string.home_pinned_clips),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        item {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(pinnedClips, key = { "pin_${it.id}" }) { clip ->
              Surface(
                onClick = { onOpenClipDetails(clip.id) },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.width(230.dp).testTag("pinned_card_${clip.id}"),
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = clip.title.ifBlank { categoryMap[clip.categoryId]?.name ?: "Pinned" },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = if (clip.isSensitive) "•••••• (Encrypted)" else clip.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  OutlinedButton(
                    onClick = { onCopyClip(clip) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  ) {
                    Text(stringResource(R.string.action_copy), style = MaterialTheme.typography.labelMedium)
                  }
                }
              }
            }
          }
        }
      }

      // Recent Section Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.home_recent_clips),
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text = "${clips.size}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }

      if (recentClips.isEmpty()) {
        item {
          EmptyStateView(
            title = stringResource(R.string.empty_clips_title),
            subtitle = stringResource(R.string.empty_clips_subtitle),
            actionLabel = stringResource(R.string.action_add_clip),
            onAction = onAddNewClip,
          )
        }
      } else {
        items(recentClips, key = { it.id }) { clip ->
          ClipCard(
            clip = clip,
            category = categoryMap[clip.categoryId],
            onClick = { onOpenClipDetails(clip.id) },
            onCopyClick = { onCopyClip(clip) },
            onPinToggle = { onTogglePin(clip) },
            onFavoriteToggle = { onToggleFavorite(clip) },
          )
        }
      }

      item { Spacer(modifier = Modifier.height(72.dp)) }
    }
  }
}
