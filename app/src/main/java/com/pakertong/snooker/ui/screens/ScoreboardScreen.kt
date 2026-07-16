package com.pakertong.snooker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pakertong.snooker.model.SnookerBall
import com.pakertong.snooker.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(vm: GameViewModel, onEndMatch: () -> Unit) {
    var showFoulSheet by remember { mutableStateOf(false) }
    var showRedCountMenu by remember { mutableStateOf(false) }
    var showEndConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF1a1a2e),
        topBar = {
            TopAppBar(
                title = { Text(formatTime(vm.matchStartTime), color = Color.White.copy(alpha = 0.7f)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0f0f23)),
                actions = {
                    IconButton(onClick = { vm.resetGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null,
                            tint = Color.White.copy(alpha = if (vm.isGameActive) 0.6f else 0.2f))
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
                    PlayerCard(player = player, isCurrent = isCurrent,
                        rank = vm.sortedPlayers.indexOf(player) + 1)
                }
            }

            // Controls
            Column(
                modifier = Modifier.background(Color(0xFF0f0f23)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {
                    InfoChip("Reds", "${vm.redsRemaining}", Color(0xFFFF4500))
                    Spacer(modifier = Modifier.width(12.dp))
                    InfoChip("Table", "${vm.tableRemaining}", Color(0xFFFFD700))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(vm.currentPlayer.color)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${vm.currentPlayer.name}'s turn", color = Color(vm.currentPlayer.color), fontWeight = FontWeight.SemiBold)
                    if (vm.isFreeBallActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Free Ball!", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Green.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                BallButtonRow(vm = vm, onLongPressRed = { showRedCountMenu = true })
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    BottomChip("Undo", Icons.Default.Undo, Color.White.copy(alpha = 0.7f), vm.hasUndo) { vm.undo() }
                    BottomChip("Foul", Icons.Default.WarningAmber, Color(0xFFFF9800), true) { showFoulSheet = true }
                    BottomChip("End", Icons.Default.Flag, Color.White.copy(alpha = 0.6f), true) { showEndConfirm = true }
                }
            }
        }
    }

    if (showFoulSheet) {
        FoulSheetDialog(vm = vm, onDismiss = { showFoulSheet = false })
    }
    if (showRedCountMenu) {
        Dialog(onDismissRequest = { showRedCountMenu = false }) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Red Ball x ?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    (2..5).forEach { count ->
                        TextButton(onClick = { showRedCountMenu = false; vm.scoreMultipleReds(count) }, modifier = Modifier.fillMaxWidth()) {
                            Text("$count Reds (${count}pts)", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    TextButton(onClick = { showRedCountMenu = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text("End Match?", color = Color.White) },
            text = { Text("Saved to history", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { showEndConfirm = false; onEndMatch() }) {
                    Text("Save & End", color = Color(0xFFFF4500))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) {
                    Text("Continue", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF16213e)
        )
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
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Miss", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
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
                Text("1", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text("Red", fontSize = 10.sp,
                color = if (disabled) Color.White.copy(alpha = 0.2f) else Color.White)
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
                    color = if (ball == SnookerBall.YELLOW) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold)
            }
            Text(ball.name, fontSize = 10.sp,
                color = if (disabled) Color.White.copy(alpha = 0.2f) else Color.White)
        }
    }
}

@Composable
fun PlayerCard(player: com.pakertong.snooker.model.Player, isCurrent: Boolean, rank: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(player.color).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$rank", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(player.color)))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (player.highestBreak > 0)
                    Text("Highest Break: ${player.highestBreak}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
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
        Text("$label: $value", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun BottomChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = color)
    }
}

fun formatTime(startMs: Long): String {
    val elapsed = (System.currentTimeMillis() - startMs) / 1000
    val m = elapsed / 60
    val s = elapsed % 60
    return "%02d:%02d".format(m, s)
}
