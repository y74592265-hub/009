package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.DiscordConfig
import com.example.domain.model.GoogleDriveConfig
import com.example.domain.model.SyncProviderId
import com.example.domain.model.TelegramConfig
import com.example.integrations.gdrive.DriveFileDto
import com.example.ui.components.formatRelativeTime
import com.example.ui.viewmodel.SyncIntegrationsViewModel

@Composable
fun IntegrationsScreen(
  viewModel: SyncIntegrationsViewModel,
  telegramConfig: TelegramConfig,
  discordConfig: DiscordConfig,
  driveConfig: GoogleDriveConfig,
  driveFolders: List<DriveFileDto>,
  isBusy: Boolean,
  modifier: Modifier = Modifier,
) {
  val consentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
      viewModel.onDriveConsentResult(result.data)
    }

  LaunchedEffect(Unit) {
    viewModel.pendingConsentIntent.collect { pendingIntent ->
      consentLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
    }
  }

  var showFolderPickerDialog by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .widthIn(max = 760.dp)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      TelegramIntegrationCard(
        config = telegramConfig,
        isBusy = isBusy,
        onSave = viewModel::saveTelegramConfig,
        onDetectChatId = viewModel::detectTelegramChatId,
        onTest = viewModel::testTelegramConnection,
        onSyncNow = { viewModel.syncProviderNow(SyncProviderId.TELEGRAM) },
      )

      DiscordIntegrationCard(
        config = discordConfig,
        isBusy = isBusy,
        onSave = viewModel::saveDiscordConfig,
        onTest = viewModel::testDiscordConnection,
        onSyncNow = { viewModel.syncProviderNow(SyncProviderId.DISCORD) },
      )

      GoogleDriveIntegrationCard(
        config = driveConfig,
        isBusy = isBusy,
        onConnect = { viewModel.connectGoogleDrive(!driveConfig.useAppDataFolder) },
        onSaveConfig = viewModel::saveDriveConfig,
        onOpenFolderPicker = {
          viewModel.loadDriveFolders()
          showFolderPickerDialog = true
        },
        onDisconnect = viewModel::disconnectGoogleDrive,
      )

      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  if (showFolderPickerDialog) {
    var newFolderName by remember { mutableStateOf("Smart Clipboard Backups") }
    AlertDialog(
      onDismissRequest = { showFolderPickerDialog = false },
      title = { Text("Select Backup Location") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedButton(
            onClick = {
              viewModel.saveDriveConfig(
                driveConfig.copy(
                  useAppDataFolder = true,
                  selectedFolderId = null,
                  selectedFolderName = "App Data Folder",
                )
              )
              showFolderPickerDialog = false
            },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Use Private App Data Folder (Recommended)")
          }
          HorizontalDivider()
          Text("Or pick / create a Drive folder:", style = MaterialTheme.typography.labelLarge)
          driveFolders.forEach { folder ->
            TextButton(
              onClick = {
                viewModel.saveDriveConfig(
                  driveConfig.copy(
                    useAppDataFolder = false,
                    selectedFolderId = folder.id,
                    selectedFolderName = folder.name,
                  )
                )
                showFolderPickerDialog = false
              },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(folder.name)
            }
          }
          OutlinedTextField(
            value = newFolderName,
            onValueChange = { newFolderName = it },
            label = { Text("New Folder Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Button(
            onClick = { viewModel.createDriveBackupFolder(newFolderName) },
            enabled = newFolderName.isNotBlank() && !isBusy,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Create & Select Folder")
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showFolderPickerDialog = false }) {
          Text(stringResource(R.string.action_close))
        }
      },
    )
  }
}

@Composable
private fun TelegramIntegrationCard(
  config: TelegramConfig,
  isBusy: Boolean,
  onSave: (TelegramConfig) -> Unit,
  onDetectChatId: (String) -> Unit,
  onTest: () -> Unit,
  onSyncNow: () -> Unit,
) {
  var enabled by remember(config.enabled) { mutableStateOf(config.enabled) }
  var botToken by remember(config.botToken) { mutableStateOf(config.botToken) }
  var chatId by remember(config.chatId) { mutableStateOf(config.chatId) }
  var autoSync by remember(config.autoSync) { mutableStateOf(config.autoSync) }
  var showToken by remember { mutableStateOf(false) }

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(Icons.Filled.Send, null, tint = MaterialTheme.colorScheme.primary)
          Spacer(Modifier.width(10.dp))
          Column {
            Text(stringResource(R.string.telegram_title), style = MaterialTheme.typography.titleLarge)
            Text(
              stringResource(R.string.telegram_subtitle),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Switch(
          checked = enabled,
          onCheckedChange = {
            enabled = it
            onSave(config.copy(enabled = it, botToken = botToken, chatId = chatId, autoSync = autoSync))
          },
          modifier = Modifier.testTag("telegram_enable_switch"),
        )
      }

      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
      ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
          Icon(Icons.Filled.Info, null, Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.telegram_notice),
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }

      OutlinedTextField(
        value = botToken,
        onValueChange = { botToken = it },
        label = { Text("Bot Token (from @BotFather)") },
        singleLine = true,
        visualTransformation =
          if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
          IconButton(onClick = { showToken = !showToken }) {
            Icon(
              if (showToken) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
              contentDescription = "Toggle token visibility",
            )
          }
        },
        modifier = Modifier.fillMaxWidth().testTag("telegram_token_input"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = chatId,
          onValueChange = { chatId = it },
          label = { Text("Chat ID") },
          singleLine = true,
          modifier = Modifier.weight(1f).testTag("telegram_chat_id_input"),
        )
        OutlinedButton(
          onClick = { onDetectChatId(botToken) },
          enabled = botToken.isNotBlank() && !isBusy,
        ) {
          Text("Detect ID")
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Include in Auto-Sync", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = autoSync, onCheckedChange = { autoSync = it })
      }

      if (config.lastStatus.isNotBlank() || config.lastSyncAt != null) {
        val syncTime = config.lastSyncAt?.let { " • Last sync ${formatRelativeTime(it)}" }.orEmpty()
        Text(
          text = "Status: ${config.lastStatus}$syncTime",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onSave(config.copy(enabled = enabled, botToken = botToken.trim(), chatId = chatId.trim(), autoSync = autoSync))
            onTest()
          },
          enabled = botToken.isNotBlank() && !isBusy,
          modifier = Modifier.weight(1f).testTag("telegram_test_btn"),
        ) {
          Text("Save & Test")
        }
        OutlinedButton(
          onClick = {
            onSave(config.copy(enabled = enabled, botToken = botToken.trim(), chatId = chatId.trim(), autoSync = autoSync))
            onSyncNow()
          },
          enabled = enabled && botToken.isNotBlank() && chatId.isNotBlank() && !isBusy,
          modifier = Modifier.weight(1f),
        ) {
          Text(stringResource(R.string.action_sync_now))
        }
      }
    }
  }
}

@Composable
private fun DiscordIntegrationCard(
  config: DiscordConfig,
  isBusy: Boolean,
  onSave: (DiscordConfig) -> Unit,
  onTest: () -> Unit,
  onSyncNow: () -> Unit,
) {
  var enabled by remember(config.enabled) { mutableStateOf(config.enabled) }
  var webhookUrl by remember(config.webhookUrl) { mutableStateOf(config.webhookUrl) }
  var botToken by remember(config.botToken) { mutableStateOf(config.botToken) }
  var channelId by remember(config.channelId) { mutableStateOf(config.channelId) }
  var autoSync by remember(config.autoSync) { mutableStateOf(config.autoSync) }

  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(Icons.Filled.Forum, null, tint = MaterialTheme.colorScheme.secondary)
          Spacer(Modifier.width(10.dp))
          Column {
            Text(stringResource(R.string.discord_title), style = MaterialTheme.typography.titleLarge)
            Text(
              stringResource(R.string.discord_subtitle),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Switch(
          checked = enabled,
          onCheckedChange = {
            enabled = it
            onSave(
              config.copy(
                enabled = it,
                webhookUrl = webhookUrl,
                botToken = botToken,
                channelId = channelId,
                autoSync = autoSync,
              )
            )
          },
          modifier = Modifier.testTag("discord_enable_switch"),
        )
      }

      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small,
      ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
          Icon(Icons.Filled.Info, null, Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.discord_notice),
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }

      OutlinedTextField(
        value = webhookUrl,
        onValueChange = { webhookUrl = it },
        label = { Text("Outgoing Webhook URL (Send only)") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().testTag("discord_webhook_input"),
      )

      OutlinedTextField(
        value = botToken,
        onValueChange = { botToken = it },
        label = { Text("Authorized Bot Token (Optional, for Receiving)") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth().testTag("discord_bot_token_input"),
      )

      OutlinedTextField(
        value = channelId,
        onValueChange = { channelId = it },
        label = { Text("Discord Channel ID (for Bot)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("discord_channel_input"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Include in Auto-Sync", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = autoSync, onCheckedChange = { autoSync = it })
      }

      if (config.lastStatus.isNotBlank()) {
        Text(
          text = "Status: ${config.lastStatus}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onSave(
              config.copy(
                enabled = enabled,
                webhookUrl = webhookUrl.trim(),
                botToken = botToken.trim(),
                channelId = channelId.trim(),
                autoSync = autoSync,
              )
            )
            onTest()
          },
          enabled = !isBusy,
          modifier = Modifier.weight(1f).testTag("discord_test_btn"),
        ) {
          Text("Save & Test")
        }
        OutlinedButton(
          onClick = {
            onSave(
              config.copy(
                enabled = enabled,
                webhookUrl = webhookUrl.trim(),
                botToken = botToken.trim(),
                channelId = channelId.trim(),
                autoSync = autoSync,
              )
            )
            onSyncNow()
          },
          enabled = enabled && !isBusy,
          modifier = Modifier.weight(1f),
        ) {
          Text(stringResource(R.string.action_sync_now))
        }
      }
    }
  }
}

@Composable
private fun GoogleDriveIntegrationCard(
  config: GoogleDriveConfig,
  isBusy: Boolean,
  onConnect: () -> Unit,
  onSaveConfig: (GoogleDriveConfig) -> Unit,
  onOpenFolderPicker: () -> Unit,
  onDisconnect: () -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CloudDone, null, tint = MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.width(10.dp))
        Column {
          Text(stringResource(R.string.drive_title), style = MaterialTheme.typography.titleLarge)
          Text(
            stringResource(R.string.drive_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (config.accountEmail.isNotBlank()) {
        Text(
          text = "Signed in: ${config.accountEmail}",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
      }
      Text(
        text = "Folder: ${config.selectedFolderName}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      config.lastBackupAt?.let {
        Text(
          text = "Last Drive Backup: ${formatRelativeTime(it)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Encrypt Drive Backups (.scbak)", style = MaterialTheme.typography.bodyMedium)
        Switch(
          checked = config.encryptBackups,
          onCheckedChange = { onSaveConfig(config.copy(encryptBackups = it)) },
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = onConnect,
          enabled = !isBusy,
          modifier = Modifier.weight(1f).testTag("connect_drive_btn"),
        ) {
          Text(if (config.enabled) "Re-Authorize Drive" else "Connect Google Drive")
        }
        OutlinedButton(
          onClick = onOpenFolderPicker,
          enabled = !isBusy,
          modifier = Modifier.weight(1f).testTag("drive_folder_btn"),
        ) {
          Text("Choose Folder")
        }
      }
      if (config.enabled) {
        TextButton(onClick = onDisconnect, modifier = Modifier.align(Alignment.End)) {
          Text("Disconnect Drive", color = MaterialTheme.colorScheme.error)
        }
      }
    }
  }
}
