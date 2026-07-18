package com.pakertong.snooker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    val uniqueDates = remember(matches) {
        matches.map { dateKey(it.date) }.distinct()
    }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val filteredMatches = remember(matches, selectedDate) {
        if (selectedDate == null) matches else matches.filter { dateKey(it.date) == selectedDate }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
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
                Text(LocalizationManager.str("history.title"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (matches.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            // Date filter chips
            if (uniqueDates.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected = selectedDate == null
                    FilterChip(
                        label = LocalizationManager.str("history.showAll"),
                        selected = allSelected,
                        onClick = { selectedDate = null }
                    )
                    uniqueDates.forEach { dateStr ->
                        val cal = Calendar.getInstance().apply {
                            val parts = dateStr.split("-").map { it.toInt() }
                            set(parts[0], parts[1] - 1, parts[2])
                        }
                        val label = String.format("%02d/%02d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
                        FilterChip(
                            label = label,
                            selected = selectedDate == dateStr,
                            onClick = { selectedDate = dateStr }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(LocalizationManager.str("history.empty"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMatches, key = { it.id }) { match ->
                        SwipeToDeleteItem(
                            onDelete = { onDelete(match.id) }
                        ) {
                            MatchCard(match = match, onClick = { selectedMatch = match })
                        }
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
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color(0xFFFF4500) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteItem(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = androidx.compose.material.rememberDismissState()
    if (dismissState.isDismissed(androidx.compose.material.DismissDirection.EndToStart)) {
        LaunchedEffect(dismissState) {
            onDelete()
            dismissState.reset()
        }
    }
    androidx.compose.material.SwipeToDismiss(
        state = dismissState,
        directions = setOf(androidx.compose.material.DismissDirection.EndToStart),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.6f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        dismissContent = { content() }
    )
}

@Composable
fun MatchCard(match: MatchRecord, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val durationStr = formatDuration(match.duration)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(match.winnerName, color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(dateFormat.format(Date(match.date)),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                Text(durationStr, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            match.players.forEachIndexed { i, p ->
                val color = listOf(0xFFFF4500, 0xFF1E90FF, 0xFF32CD32, 0xFFFFD700)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(color[i % 4])))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(p.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f))
                    Text("${p.score}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val context = LocalContext.current
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
                Text("用时: ${formatDuration(match.duration)}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Player scores
                match.players.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(p.name, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
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

                // Events timeline
                if (match.events.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(LocalizationManager.str("detail.events"),
                        color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        match.events.forEach { event ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                val dotColor = if (event.isFoul) Color(0xFFFF4500) else Color(0xFFFFD700)
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(event.playerName, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                                    modifier = Modifier.widthIn(min = 40.dp, max = 80.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val ballName = LocalizationManager.str(event.ballLocKey)
                                Text("+${event.points} - $ballName", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp,
                                    modifier = Modifier.weight(1f))
                                Text(timeFormat.format(Date(event.timestamp)),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                            }
                        }
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
                    OutlinedButton(onClick = {
                        val shareText = buildString {
                            appendLine("${match.winnerName} - ${dateFormat.format(Date(match.date))}")
                            appendLine("Duration: ${formatDuration(match.duration)}")
                            match.players.forEach { p ->
                                appendLine("${p.name}: ${p.score} (Highest Break: ${p.highestBreak})")
                            }
                            appendLine("Best Break: ${match.bestBreak}")
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, null))
                    },
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

private fun dateKey(dateMs: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
    return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
}

private fun formatDuration(seconds: Long): String {
    val m = (seconds / 60).toInt()
    val s = (seconds % 60).toInt()
    return "%02d:%02d".format(m, s)
}
