package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipContentType
import com.example.domain.model.ClipItem
import com.example.domain.model.SyncProviderId
import com.example.ui.components.AddEditClipSheet
import com.example.ui.components.ContentTypeBadge
import com.example.ui.theme.FavoriteGold
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PinAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClipDetailsScreen(
  clip: ClipItem?,
  categories: List<Category>,
  onCopy: (ClipItem) -> Unit,
  onUpdateClip: (ClipItem) -> Unit,
  onTogglePin: (ClipItem) -> Unit,
  onToggleFavorite: (ClipItem) -> Unit,
  onDelete: (Long) -> Unit,
  onPushToProvider: (ClipItem, SyncProviderId) -> Unit,
  onPasteSystemClipboard: () -> String?,
  modifier: Modifier = Modifier,
) {
  if (clip == null) {
    Column(
      modifier = modifier.fillMaxSize().padding(32.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text("Clip not found", style = MaterialTheme.typography.titleLarge)
    }
    return
  }

  val context = LocalContext.current
  var showEditSheet by remember { mutableStateOf(false) }
  var revealSensitive by remember { mutableStateOf(!clip.isSensitive) }
  val category = categories.find { it.id == clip.categoryId }
  val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .widthIn(max = 760.dp)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top metadata row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ContentTypeBadge(clip.contentType)
          AssistChip(onClick = { showEditSheet = true }, label = { Text(category?.name ?: "Personal") })
          if (clip.isSensitive) {
            AssistChip(
              onClick = { revealSensitive = !revealSensitive },
              leadingIcon = { Icon(Icons.Filled.Lock, null, Modifier.size(16.dp)) },
              label = { Text(if (revealSensitive) "Hide" else "Reveal") },
            )
          }
        }
        Row {
          IconButton(onClick = { onTogglePin(clip) }, modifier = Modifier.testTag("details_pin_btn")) {
            Icon(
              imageVector = if (clip.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
              contentDescription = stringResource(R.string.action_pin),
              tint = if (clip.isPinned) PinAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(
            onClick = { onToggleFavorite(clip) },
            modifier = Modifier.testTag("details_fav_btn"),
          ) {
            Icon(
              imageVector = if (clip.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
              contentDescription = stringResource(R.string.action_favorite),
              tint =
                if (clip.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      if (clip.title.isNotBlank()) {
        Text(text = clip.title, style = MaterialTheme.typography.headlineSmall)
      }

      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        SelectionContainer {
          Text(
            text =
              if (clip.isSensitive && !revealSensitive) {
                "•••••••• •••••••• ••••••••\nTap 'Reveal' above to decrypt locally."
              } else {
                clip.content
              },
            style =
              if (clip.contentType == ClipContentType.CODE) {
                MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily)
              } else {
                MaterialTheme.typography.bodyLarge
              },
            modifier = Modifier.fillMaxWidth().padding(18.dp),
          )
        }
      }

      if (clip.tags.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          clip.tags.forEach { tag ->
            Surface(
              shape = MaterialTheme.shapes.small,
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }

      // Primary actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = { onCopy(clip) },
          modifier = Modifier.weight(1f).testTag("details_copy_btn"),
        ) {
          Icon(Icons.Filled.ContentCopy, null, Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.action_copy))
        }
        OutlinedButton(
          onClick = { showEditSheet = true },
          modifier = Modifier.weight(1f).testTag("details_edit_btn"),
        ) {
          Icon(Icons.Filled.Edit, null, Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.action_edit))
        }
      }

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = {
            val sendIntent =
              Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, clip.content)
              }
            context.startActivity(Intent.createChooser(sendIntent, "Share Clip"))
          },
          modifier = Modifier.testTag("details_share_btn"),
        ) {
          Icon(Icons.Filled.Share, null, Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.action_share))
        }

        if (clip.contentType == ClipContentType.LINK) {
          OutlinedButton(
            onClick = {
              val url =
                if (clip.content.startsWith("http")) clip.content.trim()
                else "https://${clip.content.trim()}"
              runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            }
          ) {
            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Open URL")
          }
        }

        if (!clip.isSensitive) {
          OutlinedButton(onClick = { onPushToProvider(clip, SyncProviderId.TELEGRAM) }) {
            Icon(Icons.Filled.Send, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Sync Telegram")
          }
          OutlinedButton(onClick = { onPushToProvider(clip, SyncProviderId.DISCORD) }) {
            Icon(Icons.Filled.Send, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Sync Discord")
          }
        }
      }

      HorizontalDivider()

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Created: ${dateFmt.format(Date(clip.createdAt))}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Updated: ${dateFmt.format(Date(clip.updatedAt))}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          text = "Source: ${clip.source.name} • Hash: ${clip.contentHash.take(12)}…",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      OutlinedButton(
        onClick = { onDelete(clip.id) },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.fillMaxWidth().testTag("details_delete_btn"),
      ) {
        Icon(Icons.Filled.Delete, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.action_delete))
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showEditSheet) {
    AddEditClipSheet(
      categories = categories,
      initialTitle = clip.title,
      initialContent = clip.content,
      initialCategoryId = clip.categoryId,
      initialTags = clip.tags.joinToString(", "),
      initialSensitive = clip.isSensitive,
      initialType = clip.contentType,
      isEditing = true,
      onPasteSystemClipboard = onPasteSystemClipboard,
      onDismiss = { showEditSheet = false },
      onSave = { title, content, catId, tags, isSens, type ->
        showEditSheet = false
        onUpdateClip(
          clip.copy(
            title = title,
            content = content,
            categoryId = catId,
            tags = tags,
            isSensitive = isSens,
            contentType = type ?: clip.contentType,
          )
        )
      },
    )
  }
}
