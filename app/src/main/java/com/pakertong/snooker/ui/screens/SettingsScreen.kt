package com.pakertong.snooker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pakertong.snooker.model.AppLanguage
import com.pakertong.snooker.model.LocalizationManager
import com.pakertong.snooker.ui.theme.AppTheme
import com.pakertong.snooker.ui.theme.ThemeManager

@Composable
fun SettingsScreen(onClearData: () -> Unit) {
    var showDeleteAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(LocalizationManager.str("settings.title"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(32.dp))

        // Language section
        SectionTitle(LocalizationManager.str("settings.language"))
        Spacer(modifier = Modifier.height(8.dp))
        LanguageOption(AppLanguage.SYSTEM, "${LocalizationManager.str("settings.followSystem")} (${detectSystemLang()})")
        LanguageOption(AppLanguage.ZH_CN, "中文")
        LanguageOption(AppLanguage.JA_JP, "日本語")
        LanguageOption(AppLanguage.EN, "English")

        Spacer(modifier = Modifier.height(24.dp))

        // Theme section
        SectionTitle(LocalizationManager.str("settings.theme"))
        Spacer(modifier = Modifier.height(8.dp))
        ThemeOption(AppTheme.SYSTEM, LocalizationManager.str("settings.followSystem"))
        ThemeOption(AppTheme.DARK, LocalizationManager.str("settings.dark"))
        ThemeOption(AppTheme.LIGHT, LocalizationManager.str("settings.light"))

        Spacer(modifier = Modifier.height(24.dp))

        // Data management
        SectionTitle(LocalizationManager.str("settings.data"))
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().clickable { showDeleteAll = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(LocalizationManager.str("settings.clearAll"), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(LocalizationManager.strf("settings.records", ThemeManager.matchCount),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About section
        SectionTitle(LocalizationManager.str("settings.about"))
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(LocalizationManager.str("settings.version"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text("1.0.0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 15.sp)
            }
        }
    }

    if (showDeleteAll) {
        AlertDialog(
            onDismissRequest = { showDeleteAll = false },
            title = { Text(LocalizationManager.str("settings.deleteAll"), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(LocalizationManager.strf("settings.deleteMsg", ThemeManager.matchCount),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteAll = false; onClearData() }) {
                    Text(LocalizationManager.str("settings.delete"), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAll = false }) {
                    Text(LocalizationManager.str("detail.cancel"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
}

@Composable
fun LanguageOption(language: AppLanguage, label: String) {
    val isSelected = LocalizationManager.currentLanguage == language
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.5f else 0.15f))
            .clickable { LocalizationManager.currentLanguage = language }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        RadioButton(selected = isSelected, onClick = { LocalizationManager.currentLanguage = language },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF4500)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
    }
}

@Composable
fun ThemeOption(theme: AppTheme, label: String) {
    val isSelected = ThemeManager.currentTheme == theme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.5f else 0.15f))
            .clickable { ThemeManager.currentTheme = theme }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        RadioButton(selected = isSelected, onClick = { ThemeManager.currentTheme = theme },
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF4500)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
    }
}

private fun detectSystemLang(): String {
    val locale = java.util.Locale.getDefault()
    return when {
        locale.language == "zh" -> "中文"
        locale.language == "ja" -> "日本語"
        else -> "English"
    }
}
