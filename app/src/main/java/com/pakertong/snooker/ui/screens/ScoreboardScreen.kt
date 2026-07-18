package com.pakertong.snooker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pakertong.snooker.model.LocalizationManager
import com.pakertong.snooker.model.SnookerBall
import com.pakertong.snooker.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(vm: GameViewModel, onEndMatch: () -> Unit) {
    var showFoulSheet by remember { mutableStateOf(false) }
    var showRedCountMenu by remember { mutableStateOf(false) }
    var showEndConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    var now by remember { mutableStateOf(System.currentTimeMillis()) }
                    LaunchedEffect(Unit) { while(true) { delay(1000); now = System.currentTimeMillis() } }
                    val matchElapsed = formatTime(vm.matchStartTime, now)
                    val turnElapsed = ((now - vm.turnStartTime) / 1000).toInt()
                    Text("$matchElapsed · ${turnElapsed}s", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (vm.isGameActive) 0.6f else 0.2f))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.sortedPlayers) { player ->
                    val isCurrent = player.id == vm.currentPlayer.id
                    val topScore = vm.sortedPlayers.firstOrNull()?.score ?: 0
                    val isRecent = player.id == vm.recentScorePlayerId
                    LaunchedEffect(vm.recentScorePlayerId) {
                        if (vm.recentScorePlayerId != null) { delay(600); vm.clearRecentScore() }
                    }
                    PlayerCard(
                        player = player, isCurrent = isCurrent, isRecent = isRecent,
                        rank = vm.sortedPlayers.indexOf(player) + 1, topScore = topScore,
                        onClick = { if (!isCurrent) vm.selectPlayer(player.id) }
                    )
                }
            }

            // Controls
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {
                    InfoChip(LocalizationManager.str("sb.reds"), "${vm.redsRemaining}", Color(0xFFFF4500))
                    Spacer(modifier = Modifier.width(12.dp))
                    InfoChip(LocalizationManager.str("sb.table"), "${vm.tableRemaining}", Color(0xFFFFD700))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(vm.currentPlayer.color)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${vm.currentPlayer.name} ${LocalizationManager.str("sb.shooting")}", color = Color(vm.currentPlayer.color), fontWeight = FontWeight.SemiBold)
                    if (vm.isFreeBallActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalizationManager.str("sb.freeBall"), color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Green.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                BallButtonRow(vm = vm, onLongPressRed = { showRedCountMenu = true })
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    BottomChip(LocalizationManager.str("sb.undo"), Icons.AutoMirrored.Filled.Undo, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), vm.hasUndo) { vm.undo() }
                    BottomChip(LocalizationManager.str("sb.foul"), Icons.Default.WarningAmber, Color(0xFFFF9800), true) { showFoulSheet = true }
                    BottomChip(LocalizationManager.str("sb.end"), Icons.Default.Flag, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), true) { showEndConfirm = true }
                }
            }
        }
    }

    // Auto end match when table remaining = 0 (not tied) or re-spot black potted
    LaunchedEffect(vm.matchOver) {
        if (vm.matchOver) showEndConfirm = true
    }

    if (showFoulSheet) {
        FoulSheetDialog(vm = vm, onDismiss = { showFoulSheet = false })
    }
    if (showRedCountMenu) {
        Dialog(onDismissRequest = { showRedCountMenu = false }) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(LocalizationManager.str("redCount.title"), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    (2..5).forEach { count ->
                        TextButton(onClick = { showRedCountMenu = false; vm.scoreMultipleReds(count) }, modifier = Modifier.fillMaxWidth()) {
                            Text("$count Reds (${count}pts)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    TextButton(onClick = { showRedCountMenu = false }, modifier = Modifier.align(Alignment.End)) {
                        Text(LocalizationManager.str("redCount.cancel"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset scores?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("All scores will be cleared", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showResetConfirm = false; vm.resetGame() }) {
                    Text("Reset", color = Color(0xFFFF4500))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF16213e)
        )
    }
    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text(LocalizationManager.str("sb.endTitle"), color = MaterialTheme.colorScheme.onSurface) },
            text = {
                val sorted = vm.sortedPlayers
                val winner = sorted.firstOrNull()
                Column {
                    if (winner != null) {
                        val isDraw = sorted.size >= 2 && sorted.first().score == sorted.last().score
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null,
                                tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isDraw) {
                                Text("Draw!",
                                    color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            } else {
                                Text("${winner.name} wins!",
                                    color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    sorted.forEach { p ->
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            Text(p.name, color = MaterialTheme.colorScheme.onSurface)
                            Text("${p.score}", color = Color(0xFFFF4500), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    val maxBreak = sorted.maxOfOrNull { it.highestBreak } ?: 0
                    if (maxBreak >= 147) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(LocalizationManager.str("sb.maxBreak"),
                            color = Color(0xFFFF4500), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(LocalizationManager.str("sb.endMsg"),
                        color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showEndConfirm = false; onEndMatch() }) {
                    Text(LocalizationManager.str("sb.saveEnd"), color = Color(0xFFFF4500))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) {
                    Text(LocalizationManager.str("sb.continue"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF16213e)
        )
    }
    if (vm.showReSpotDialog) {
        Dialog(onDismissRequest = { }) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(LocalizationManager.str("respot.title"), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(LocalizationManager.str("respot.msg"),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    vm.players.forEachIndexed { i, p ->
                        Button(
                            onClick = { vm.startReSpotBlack(i) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(p.color)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) { Text(p.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BallButtonRow(vm: GameViewModel, onLongPressRed: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RedBallBtn(vm.isBallDisabled(SnookerBall.RED),
                onClick = { vm.score(SnookerBall.RED) },
                onLongClick = onLongPressRed)
            BallBtn(SnookerBall.YELLOW, vm.isBallDisabled(SnookerBall.YELLOW)) { vm.score(SnookerBall.YELLOW) }
            BallBtn(SnookerBall.GREEN, vm.isBallDisabled(SnookerBall.GREEN)) { vm.score(SnookerBall.GREEN) }
            BallBtn(SnookerBall.BROWN, vm.isBallDisabled(SnookerBall.BROWN)) { vm.score(SnookerBall.BROWN) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BallBtn(SnookerBall.BLUE, vm.isBallDisabled(SnookerBall.BLUE)) { vm.score(SnookerBall.BLUE) }
            BallBtn(SnookerBall.PINK, vm.isBallDisabled(SnookerBall.PINK)) { vm.score(SnookerBall.PINK) }
            BallBtn(SnookerBall.BLACK, vm.isBallDisabled(SnookerBall.BLACK)) { vm.score(SnookerBall.BLACK) }
            Button(onClick = { vm.missShot() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC7000).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).height(60.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    Text(LocalizationManager.str("sb.miss"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.RedBallBtn(disabled: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bgColor = animateColorAsState(
        targetValue = if (disabled) Color(0xFFDC143C).copy(alpha = 0.08f) else Color(0xFFDC143C).copy(alpha = 0.25f)
    )
    Box(
        modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(10.dp)).background(bgColor.value).combinedClickable(
            onClick = { if (!disabled) onClick() },
            onLongClick = { if (!disabled) onLongClick() }
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFDC143C)),
                contentAlignment = Alignment.Center) {
                Text("1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Text(LocalizationManager.str(SnookerBall.RED.locKey), fontSize = 10.sp,
                color = if (disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun RowScope.BallBtn(ball: SnookerBall, disabled: Boolean, onClick: () -> Unit) {
    val bgColor = animateColorAsState(
        targetValue = if (disabled) Color(ball.hexColor).copy(alpha = 0.08f) else Color(ball.hexColor).copy(alpha = 0.25f)
    )
    Button(
        onClick = { if (!disabled) onClick() },
        colors = ButtonDefaults.buttonColors(containerColor = bgColor.value),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.weight(1f).height(60.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(ball.hexColor)),
                contentAlignment = Alignment.Center) {
                Text("${ball.points}", fontSize = 10.sp,
                    color = if (ball == SnookerBall.YELLOW) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold)
            }
            Text(LocalizationManager.str(ball.locKey), fontSize = 10.sp,
                color = if (disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PlayerCard(
    player: com.pakertong.snooker.model.Player,
    isCurrent: Boolean,
    isRecent: Boolean = false,
    rank: Int,
    topScore: Int = 0,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(player.color).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null && !isCurrent) Modifier.clickable(onClick = onClick) else Modifier)
            .then(if (isRecent) Modifier.border(2.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp)) else Modifier)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$rank", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(player.color)))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row {
                    if (player.highestBreak > 0)
                        Text("Highest Break: ${player.highestBreak}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                    if (topScore > player.score) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${LocalizationManager.str("sb.behind")}: ${topScore - player.score}${LocalizationManager.str("sb.behindPts")}",
                            color = Color(0xFFFF4500).copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }
            }
            Text("${player.score}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(player.color))
        }
        if (isCurrent && player.currentBreak > 0) {
            Text("Break: ${player.currentBreak}", color = Color(0xFFFF9800), fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text("$label: $value", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
fun BottomChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = color)
    }
}

fun formatTime(startMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val elapsed = (nowMs - startMs) / 1000
    val m = elapsed / 60
    val s = elapsed % 60
    return "%02d:%02d".format(m, s)
}
