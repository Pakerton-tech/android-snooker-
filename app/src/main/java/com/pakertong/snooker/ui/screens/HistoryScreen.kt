package com.pakertong.snooker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pakertong.snooker.model.LocalizationManager
import com.pakertong.snooker.model.MatchRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(matches: List<MatchRecord>, onDelete: (String) -> Unit, onDeleteAll: () -> Unit) {
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var selectedMatch by remember { mutableStateOf<MatchRecord?>(null) }

    Scaffold(
        containerColor = Color(0xFF1a1a2e)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(LocalizationManager.str("history.title"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (matches.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            if (matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(LocalizationManager.str("history.empty"), color = Color.White.copy(alpha = 0.3f), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matches) { match ->
                        MatchCard(match = match, onClick = { selectedMatch = match })
                    }
                }
            }
        }
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text(LocalizationManager.str("detail.deleteAll"), color = Color.White) },
            text = { Text(LocalizationManager.strf("detail.deleteAllMsg", matches.size), color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showDeleteAllConfirm = false; onDeleteAll() }) {
                    Text(LocalizationManager.str("detail.delete"), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text(LocalizationManager.str("detail.cancel"), color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF16213e)
        )
    }

    selectedMatch?.let { match ->
        MatchDetailDialog(match = match, onDismiss = { selectedMatch = null },
            onDelete = { onDelete(match.id); selectedMatch = null })
    }
}

@Composable
fun MatchCard(match: MatchRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val durationStr = formatDuration(match.duration)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(match.winnerName, color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(dateFormat.format(Date(match.date)),
                        color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                Text(durationStr, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            match.players.forEachIndexed { i, p ->
                val color = listOf(0xFFFF4500, 0xFF1E90FF, 0xFF32CD32, 0xFFFFD700)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(color[i % 4])))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(p.name, color = Color.White, fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f))
                    Text("${p.score}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                if (i < match.players.lastIndex) Spacer(modifier = Modifier.height(4.dp))
            }
            if (match.bestBreak > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("最高单杆: ${match.bestBreak}", color = Color(0xFFFF9800), fontSize = 12.sp)
                if (match.bestBreak >= 147) {
                    Text(LocalizationManager.str("sb.maxBreak"),
                        color = Color(0xFFFF4500), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MatchDetailDialog(match: MatchRecord, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Winner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null,
                        tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LocalizationManager.strf("history.winner", match.winnerName), color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Date & duration
                Text(dateFormat.format(Date(match.date)),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Text("用时: ${formatDuration(match.duration)}",
                    color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Player scores
                match.players.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(p.name, color = Color.White, modifier = Modifier.weight(1f))
                        Text("${p.score}", color = Color(0xFFFF4500),
                            fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                if (match.bestBreak > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("最高单杆: ${match.bestBreak}", color = Color(0xFFFF9800), fontSize = 14.sp)
                    if (match.bestBreak >= 147) {
                        Text(LocalizationManager.str("sb.maxBreak"),
                            color = Color(0xFFFF4500), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationManager.str("detail.delete"))
                    }
                    OutlinedButton(onClick = { /* share - platform dependent */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationManager.str("detail.share"))
                    }
                    OutlinedButton(onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f))) {
                        Text(LocalizationManager.str("detail.close"))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(LocalizationManager.str("detail.deleteConfirm"), color = Color.White) },
            text = { Text(LocalizationManager.str("detail.deleteMsg"), color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text(LocalizationManager.str("detail.delete"), color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(LocalizationManager.str("detail.cancel"), color = Color.White.copy(alpha = 0.6f)) } },
            containerColor = Color(0xFF16213e)
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val m = (seconds / 60).toInt()
    val s = (seconds % 60).toInt()
    return "%02d:%02d".format(m, s)
}
