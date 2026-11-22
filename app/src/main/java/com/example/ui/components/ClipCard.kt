package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.Category
import com.example.domain.model.ClipContentType
import com.example.domain.model.ClipItem
import com.example.ui.theme.FavoriteGold
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PinAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ClipCard(
  clip: ClipItem,
  category: Category?,
  isSelected: Boolean = false,
  inSelectionMode: Boolean = false,
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  onCopyClick: () -> Unit,
  onPinToggle: () -> Unit,
  onFavoriteToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val border =
    if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

  ElevatedCard(
    modifier =
      modifier
        .fillMaxWidth()
        .testTag("clip_card_${clip.id}")
        .clip(MaterialTheme.shapes.medium)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    shape = MaterialTheme.shapes.medium,
    colors =
      CardDefaults.elevatedCardColors(
        containerColor =
          if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          else MaterialTheme.colorScheme.surface
      ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (clip.isPinned) 3.dp else 1.dp),
  ) {
    Surface(
      color = Color.Transparent,
      border = border,
      shape = MaterialTheme.shapes.medium,
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Header row: Category dot + name + type badge + selection/pin/fav indicators
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
            } else {
              Box(
                modifier =
                  Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(category?.colorHex ?: 0xFF3B82F6))
              )
              Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
              text = category?.name ?: "Personal",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(8.dp))
            ContentTypeBadge(contentType = clip.contentType)

            if (clip.isSensitive) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Encrypted",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(15.dp),
              )
            }
          }

          Text(
            text = formatRelativeTime(clip.updatedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        if (clip.title.isNotBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = clip.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content preview (monospace for CODE / LINK, clean body for TEXT)
        val previewText =
          if (clip.isSensitive) {
            "•••••••• •••••••• (${clip.content.length} chars encrypted)"
          } else {
            clip.content
          }

        Text(
          text = previewText,
          style =
            if (clip.contentType == ClipContentType.CODE) {
              MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily)
            } else {
              MaterialTheme.typography.bodyLarge
            },
          color =
            if (clip.isSensitive) MaterialTheme.colorScheme.tertiary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
          maxLines = 4,
          overflow = TextOverflow.Ellipsis,
        )

        if (clip.tags.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            clip.tags.take(4).forEach { tag ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
              ) {
                Text(
                  text = "#$tag",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }
          }
        }

        if (!inSelectionMode) {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
          ) {
            IconButton(
              onClick = onPinToggle,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("pin_button_${clip.id}"),
            ) {
              Icon(
                imageVector = if (clip.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription =
                  stringResource(if (clip.isPinned) R.string.action_unpin else R.string.action_pin),
                tint = if (clip.isPinned) PinAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
            IconButton(
              onClick = onFavoriteToggle,
              modifier =
                Modifier.minimumInteractiveComponentSize().testTag("favorite_button_${clip.id}"),
            ) {
              Icon(
                imageVector = if (clip.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription =
                  stringResource(
                    if (clip.isFavorite) R.string.action_unfavorite else R.string.action_favorite
                  ),
                tint =
                  if (clip.isFavorite) FavoriteGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            AssistChip(
              onClick = onCopyClick,
              modifier = Modifier.testTag("copy_button_${clip.id}"),
              leadingIcon = {
                Icon(
                  imageVector = Icons.Filled.ContentCopy,
                  contentDescription = stringResource(R.string.action_copy),
                  modifier = Modifier.size(16.dp),
                )
              },
              label = {
                Text(
                  text = stringResource(R.string.action_copy),
                  fontWeight = FontWeight.Medium,
                )
              },
              colors =
                AssistChipDefaults.assistChipColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                  labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                  leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
          }
        }
      }
    }
  }
}

@Composable
fun ContentTypeBadge(contentType: ClipContentType) {
  val icon =
    when (contentType) {
      ClipContentType.LINK -> Icons.Filled.Link
      ClipContentType.CODE -> Icons.Filled.Code
      ClipContentType.ADDRESS -> Icons.Filled.Place
      ClipContentType.NOTE -> Icons.Filled.Notes
      ClipContentType.TEXT -> null
    }
  val label =
    when (contentType) {
      ClipContentType.LINK -> stringResource(R.string.type_link)
      ClipContentType.CODE -> stringResource(R.string.type_code)
      ClipContentType.ADDRESS -> stringResource(R.string.type_address)
      ClipContentType.NOTE -> stringResource(R.string.type_note)
      ClipContentType.TEXT -> stringResource(R.string.type_text)
    }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}

fun formatRelativeTime(timestamp: Long): String {
  val diff = System.currentTimeMillis() - timestamp
  return when {
    diff < 60_000L -> "Now"
    diff < 3_600_000L -> "${diff / 60_000L}m"
    diff < 86_400_000L -> "${diff / 3_600_000L}h"
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
  }
}
