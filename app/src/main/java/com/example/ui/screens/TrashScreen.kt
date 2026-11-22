package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.ClipItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRelativeTime

@Composable
fun TrashScreen(
  trashedClips: List<ClipItem>,
  onRestore: (List<Long>) -> Unit,
  onDeleteForever: (List<Long>) -> Unit,
  onEmptyTrash: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showEmptyConfirm by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp)) {
      if (trashedClips.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${trashedClips.size} item(s) in Trash",
            style = MaterialTheme.typography.titleMedium,
          )
          OutlinedButton(
            onClick = { showEmptyConfirm = true },
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("empty_trash_btn"),
          ) {
            Text("Empty Trash")
          }
        }
      }

      if (trashedClips.isEmpty()) {
        EmptyStateView(
          title = stringResource(R.string.empty_trash_title),
          subtitle = stringResource(R.string.empty_trash_subtitle),
          showArtwork = false,
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(trashedClips, key = { it.id }) { clip ->
            ElevatedCard(
              modifier = Modifier.fillMaxWidth().testTag("trash_item_${clip.id}"),
              colors =
                CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = if (clip.isSensitive) "•••••• (Encrypted)" else clip.content,
                  style = MaterialTheme.typography.bodyMedium,
                  maxLines = 3,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Deleted ${formatRelativeTime(clip.trashedAt ?: clip.updatedAt)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                ) {
                  TextButton(
                    onClick = { onRestore(listOf(clip.id)) },
                    modifier = Modifier.testTag("restore_clip_${clip.id}"),
                  ) {
                    Icon(Icons.Filled.RestoreFromTrash, null)
                    Text(stringResource(R.string.action_restore))
                  }
                  TextButton(
                    onClick = { onDeleteForever(listOf(clip.id)) },
                    modifier = Modifier.testTag("delete_forever_${clip.id}"),
                  ) {
                    Icon(
                      Icons.Filled.DeleteForever,
                      null,
                      tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                      stringResource(R.string.action_delete_forever),
                      color = MaterialTheme.colorScheme.error,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showEmptyConfirm) {
    AlertDialog(
      onDismissRequest = { showEmptyConfirm = false },
      title = { Text("Empty Trash?") },
      text = { Text("Permanently delete all ${trashedClips.size} trashed clips?") },
      confirmButton = {
        TextButton(
          onClick = {
            showEmptyConfirm = false
            onEmptyTrash()
          }
        ) {
          Text(stringResource(R.string.action_delete_forever), color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEmptyConfirm = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }
}
