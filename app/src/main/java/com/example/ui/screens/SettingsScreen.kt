package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import com.example.core.preferences.AppLanguage
import com.example.core.preferences.ThemeMode
import com.example.core.preferences.UserSettings

@Composable
fun SettingsScreen(
  settings: UserSettings,
  onThemeChange: (ThemeMode) -> Unit,
  onLanguageChange: (AppLanguage) -> Unit,
  onAppLockToggle: (Boolean) -> Unit,
  onAutoCaptureToggle: (Boolean) -> Unit,
  onRetentionDaysChange: (Int) -> Unit,
  onClearSystemClipboard: () -> Unit,
  onWipeAllData: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showWipeConfirm by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .widthIn(max = 760.dp)
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Appearance
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Appearance & Language", style = MaterialTheme.typography.titleLarge)

          Text("Theme", style = MaterialTheme.typography.labelLarge)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
              FilterChip(
                selected = settings.themeMode == mode,
                onClick = { onThemeChange(mode) },
                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.testTag("theme_${mode.name.lowercase()}"),
              )
            }
          }

          Text("Language (LTR / RTL)", style = MaterialTheme.typography.labelLarge)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { lang ->
              val label =
                when (lang) {
                  AppLanguage.SYSTEM -> "System"
                  AppLanguage.ENGLISH -> "English"
                  AppLanguage.ARABIC -> "العربية"
                }
              FilterChip(
                selected = settings.language == lang,
                onClick = { onLanguageChange(lang) },
                label = { Text(label) },
                modifier = Modifier.testTag("lang_${lang.name.lowercase()}"),
              )
            }
          }
        }
      }

      // Privacy & Security
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Security & Privacy", style = MaterialTheme.typography.titleLarge)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text("Biometric / Device App Lock", style = MaterialTheme.typography.titleMedium)
              Text(
                "Require authentication when opening Smart Clipboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = settings.appLockEnabled,
              onCheckedChange = onAppLockToggle,
              modifier = Modifier.testTag("app_lock_switch"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
              Text("Foreground Clipboard Detection", style = MaterialTheme.typography.titleMedium)
              Text(
                "Offer to save newly copied text when opening the app (no background spying)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = settings.autoCaptureOnForeground,
              onCheckedChange = onAutoCaptureToggle,
              modifier = Modifier.testTag("auto_capture_switch"),
            )
          }

          Text("History Retention", style = MaterialTheme.typography.labelLarge)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "Forever", 7 to "7 Days", 30 to "30 Days", 90 to "90 Days").forEach {
              (days, label) ->
              FilterChip(
                selected = settings.historyRetentionDays == days,
                onClick = { onRetentionDaysChange(days) },
                label = { Text(label) },
              )
            }
          }

          OutlinedButton(
            onClick = onClearSystemClipboard,
            modifier = Modifier.fillMaxWidth().testTag("clear_sys_clipboard_btn"),
          ) {
            Text("Clear System Clipboard Now")
          }
        }
      }

      // Danger Zone
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Data Control", style = MaterialTheme.typography.titleLarge)
          Text(
            "Permanently delete all local clips, sync logs, and stored integration tokens.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          OutlinedButton(
            onClick = { showWipeConfirm = true },
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().testTag("wipe_all_data_btn"),
          ) {
            Text("Wipe All Local Data")
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))
    }
  }

  if (showWipeConfirm) {
    AlertDialog(
      onDismissRequest = { showWipeConfirm = false },
      title = { Text("Wipe All Local Data?") },
      text = {
        Text("This permanently erases every clip, Trash item, sync log, and integration credential from this device.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showWipeConfirm = false
            onWipeAllData()
          },
          modifier = Modifier.testTag("confirm_wipe_btn"),
        ) {
          Text("Wipe Forever", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showWipeConfirm = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }
}
