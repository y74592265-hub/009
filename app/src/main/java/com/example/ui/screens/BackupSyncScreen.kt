package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.preferences.UserSettings
import com.example.core.sync.SyncEngineState
import com.example.domain.model.GoogleDriveConfig
import com.example.domain.model.SyncLog
import com.example.domain.model.SyncStatus
import com.example.domain.usecase.ExportFormat
import com.example.integrations.gdrive.DriveFileDto
import com.example.ui.components.formatRelativeTime
import com.example.ui.theme.ErrorLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SyncIntegrationsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackupSyncScreen(
  viewModel: SyncIntegrationsViewModel,
  syncState: SyncEngineState,
  syncLogs: List<SyncLog>,
  userSettings: UserSettings,
  driveConfig: GoogleDriveConfig,
  driveBackups: List<DriveFileDto>,
  lastGlobalSyncAt: Long?,
  isBusy: Boolean,
  onOpenIntegrations: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var pendingExportFormat by remember { mutableStateOf<ExportFormat?>(null) }
  var pendingPassphrase by remember { mutableStateOf<String?>(null) }
  var showPassphraseDialogFor by remember { mutableStateOf<String?>(null) } // "export_enc", "drive_backup", "restore_drive:<id>", "restore_local:<raw>"
  var pendingRawLocalImport by remember { mutableStateOf<String?>(null) }

  val createDocumentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
      val fmt = pendingExportFormat ?: return@rememberLauncherForActivityResult
      if (uri == null) return@rememberLauncherForActivityResult
      scope.launch {
        runCatching {
          val text = viewModel.buildLocalExportContent(fmt, pendingPassphrase)
          context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(text) }
        }
        pendingPassphrase = null
      }
    }

  val openDocumentLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri == null) return@rememberLauncherForActivityResult
      scope.launch {
        val raw =
          context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return@launch
        if (raw.contains("SMART_CLIPBOARD_ENC_V1")) {
          pendingRawLocalImport = raw
          showPassphraseDialogFor = "restore_local"
        } else {
          viewModel.restoreLocalFile(raw, null)
        }
      }
    }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
      contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Hero Banner & Sync Engine Status
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column {
            Image(
              painter = painterResource(id = R.drawable.img_sync_cloud_banner),
              contentDescription = "Cloud Sync Banner",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(130.dp).clip(MaterialTheme.shapes.medium),
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Local-First Sync Engine", style = MaterialTheme.typography.titleLarge)
                  val lastSyncLabel =
                    lastGlobalSyncAt?.let { "Last sync: ${formatRelativeTime(it)}" }
                      ?: "Never synced"
                  Text(
                    text = syncState.lastMessage.ifBlank { lastSyncLabel },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Button(
                  onClick = { viewModel.syncAllNow() },
                  enabled = !syncState.isSyncing && !isBusy,
                  modifier = Modifier.testTag("sync_all_now_btn"),
                ) {
                  Icon(Icons.Filled.Sync, null)
                  Spacer(Modifier.width(6.dp))
                  Text(stringResource(R.string.action_sync_now))
                }
              }

              if (syncState.isSyncing || isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Background Auto-Sync (WorkManager)", style = MaterialTheme.typography.titleMedium)
                  Text(
                    "Runs every ${userSettings.autoSyncIntervalHours}h when network is available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Switch(
                  checked = userSettings.autoSyncEnabled,
                  onCheckedChange = { viewModel.toggleAutoSync(it, userSettings.autoSyncIntervalHours) },
                  modifier = Modifier.testTag("auto_sync_switch"),
                )
              }

              OutlinedButton(
                onClick = onOpenIntegrations,
                modifier = Modifier.fillMaxWidth().testTag("open_integrations_btn"),
              ) {
                Text("Configure Telegram, Discord & Google Drive")
              }
            }
          }
        }
      }

      // Local Export & Restore (SAF - Zero Storage Permissions)
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Local Export & Backup", style = MaterialTheme.typography.titleLarge)
            Text(
              "Export readable files or encrypted .scbak archives using Android Storage Access Framework.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = {
                  pendingExportFormat = ExportFormat.JSON
                  pendingPassphrase = null
                  createDocumentLauncher.launch("smart_clipboard_backup.json")
                },
                modifier = Modifier.testTag("export_json_btn"),
              ) {
                Icon(Icons.Filled.Download, null)
                Spacer(Modifier.width(4.dp))
                Text("JSON")
              }
              OutlinedButton(
                onClick = {
                  pendingExportFormat = ExportFormat.CSV
                  pendingPassphrase = null
                  createDocumentLauncher.launch("smart_clipboard_export.csv")
                },
                modifier = Modifier.testTag("export_csv_btn"),
              ) {
                Text("CSV")
              }
              OutlinedButton(
                onClick = {
                  pendingExportFormat = ExportFormat.TXT
                  pendingPassphrase = null
                  createDocumentLauncher.launch("smart_clipboard_export.txt")
                },
                modifier = Modifier.testTag("export_txt_btn"),
              ) {
                Text("TXT")
              }
              Button(
                onClick = { showPassphraseDialogFor = "export_enc" },
                modifier = Modifier.testTag("export_encrypted_btn"),
              ) {
                Text("Encrypted (.scbak)")
              }
            }

            OutlinedButton(
              onClick = { openDocumentLauncher.launch(arrayOf("*/*")) },
              modifier = Modifier.fillMaxWidth().testTag("import_local_btn"),
            ) {
              Icon(Icons.Filled.UploadFile, null)
              Spacer(Modifier.width(6.dp))
              Text("Restore from Local Backup (.json / .scbak)")
            }
          }
        }
      }

      // Google Drive Cloud Backups
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Google Drive Backups", style = MaterialTheme.typography.titleLarge)
            if (!driveConfig.enabled) {
              Text(
                "Connect Google Drive in Integrations to upload & restore cloud backups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            } else {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = {
                    if (driveConfig.encryptBackups) {
                      showPassphraseDialogFor = "drive_backup"
                    } else {
                      viewModel.backupToDriveNow(null)
                    }
                  },
                  enabled = !isBusy,
                  modifier = Modifier.weight(1f).testTag("drive_backup_now_btn"),
                ) {
                  Icon(Icons.Filled.Backup, null)
                  Spacer(Modifier.width(6.dp))
                  Text("Backup Now")
                }
                OutlinedButton(
                  onClick = { viewModel.refreshDriveBackups() },
                  enabled = !isBusy,
                  modifier = Modifier.weight(1f),
                ) {
                  Text("Refresh List")
                }
              }

              driveBackups.forEach { file ->
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(file.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                      file.modifiedTime ?: "",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  TextButton(
                    onClick = {
                      if (file.name.endsWith(".scbak")) {
                        showPassphraseDialogFor = "restore_drive:${file.id}"
                      } else {
                        viewModel.restoreFromDrive(file.id, null)
                      }
                    }
                  ) {
                    Icon(Icons.Filled.CloudDownload, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Restore")
                  }
                }
              }
            }
          }
        }
      }

      // Sync Logs Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Sync Activity Logs", style = MaterialTheme.typography.titleMedium)
          if (syncLogs.isNotEmpty()) {
            TextButton(onClick = { viewModel.clearSyncLogs() }) { Text("Clear") }
          }
        }
      }

      if (syncLogs.isEmpty()) {
        item {
          Text(
            text = "No sync events recorded yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        items(syncLogs, key = { it.id }) { log ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${log.provider.displayName} • ${log.direction.name}",
                style = MaterialTheme.typography.labelLarge,
                color = if (log.status == SyncStatus.ERROR) ErrorLight else SuccessGreen,
              )
              Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = formatRelativeTime(log.timestamp),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(48.dp)) }
    }
  }

  showPassphraseDialogFor?.let { action ->
    var passphrase by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showPassphraseDialogFor = null },
      title = { Text("Backup Passphrase") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Enter a passphrase (min 6 characters) for AES-256-GCM encryption/decryption.")
          OutlinedTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            label = { Text("Passphrase") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("backup_passphrase_input"),
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val currentAction = action
            showPassphraseDialogFor = null
            when {
              currentAction == "export_enc" -> {
                pendingExportFormat = ExportFormat.ENCRYPTED_BACKUP
                pendingPassphrase = passphrase
                createDocumentLauncher.launch("smart_clipboard_encrypted.scbak")
              }
              currentAction == "drive_backup" -> viewModel.backupToDriveNow(passphrase)
              currentAction == "restore_local" -> {
                pendingRawLocalImport?.let { viewModel.restoreLocalFile(it, passphrase) }
                pendingRawLocalImport = null
              }
              currentAction.startsWith("restore_drive:") -> {
                val fileId = currentAction.removePrefix("restore_drive:")
                viewModel.restoreFromDrive(fileId, passphrase)
              }
            }
          },
          enabled = passphrase.length >= 6,
          modifier = Modifier.testTag("confirm_passphrase_btn"),
        ) {
          Text("Confirm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPassphraseDialogFor = null }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }
}
