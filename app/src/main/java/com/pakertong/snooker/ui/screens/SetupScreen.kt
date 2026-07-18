package com.pakertong.snooker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakertong.snooker.model.GameStore
import com.pakertong.snooker.model.LocalizationManager
import com.pakertong.snooker.viewmodel.GameViewModel

@Composable
fun MainScreen(vm: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val store = remember { GameStore(context).also { it.load() } }
    var inGame by remember { mutableStateOf(false) }
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var playerCount by remember { mutableIntStateOf(2) }
    var redBallCount by remember { mutableIntStateOf(15) }
    var mainTab by remember { mutableIntStateOf(0) }

    var matches by remember { mutableStateOf(store.allMatches) }
    fun refreshMatches() { matches = store.allMatches }

    Scaffold(
        bottomBar = {
            if (!inGame) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    NavigationBarItem(
                        selected = mainTab == 0,
                        onClick = { mainTab = 0 },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        label = { Text(LocalizationManager.str("nav.game")) }
                    )
                    NavigationBarItem(
                        selected = mainTab == 1,
                        onClick = { mainTab = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text(LocalizationManager.str("nav.history")) }
                    )
                    NavigationBarItem(
                        selected = mainTab == 2,
                        onClick = { mainTab = 2 },
                        icon = { Text("⚙", fontSize = 18.sp) },
                        label = { Text(LocalizationManager.str("nav.settings")) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        when (mainTab) {
            0 -> {
                if (!inGame) {
                    SetupScreen(
                        playerCount = playerCount,
                        playerNames = playerNames,
                        redBallCount = redBallCount,
                        onPlayerCountChange = { count ->
                            playerCount = count
                            playerNames = List(count) { i -> playerNames.getOrElse(i) { "" } }
                        },
                        onNameChange = { i, name ->
                            playerNames = playerNames.toMutableList().also { it[i] = name }
                        },
                        onRedBallCountChange = { redBallCount = it },
                        onStart = {
                            vm.setupPlayers(playerNames, redBallCount)
                            inGame = true
                        }
                    )
                } else {
                    ScoreboardScreen(
                        vm = vm,
                        onEndMatch = {
                            // Save match record
                            val sortedPlayers = vm.players.sortedByDescending { it.score }
                            val record = com.pakertong.snooker.model.MatchRecord(
                                date = System.currentTimeMillis(),
                                duration = (System.currentTimeMillis() - vm.matchStartTime) / 1000,
                                players = sortedPlayers.map { p ->
                                    com.pakertong.snooker.model.PlayerSnapshot(
                                        name = p.name,
                                        score = p.score,
                                        highestBreak = p.highestBreak,
                                        colorIndex = p.colorIndex
                                    )
                                },
                                winnerName = sortedPlayers.firstOrNull()?.name ?: "",
                                events = vm.events.toList()
                            )
                            store.save(record)
                            refreshMatches()
                            inGame = false
                        }
                    )
                }
            }
            1 -> HistoryScreen(
                matches = matches,
                onDelete = { id -> store.delete(id); refreshMatches() },
                onDeleteAll = { store.deleteAll(); refreshMatches() }
            )
            2 -> SettingsScreen(
                onClearData = { store.deleteAll(); refreshMatches() }
            )
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    playerCount: Int,
    playerNames: List<String>,
    redBallCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onNameChange: (Int, String) -> Unit,
    onRedBallCountChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    val playerColors = listOf(0xFFFF4500, 0xFF1E90FF, 0xFF32CD32, 0xFFFFD700)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a2e))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(LocalizationManager.str("app.name"),
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(LocalizationManager.str("app.subtitle"),
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(32.dp))

            // Player count
            Text(LocalizationManager.str("setup.playerCount"),
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                (2..4).forEach { n ->
                    val selected = playerCount == n
                    Button(
                        onClick = { onPlayerCountChange(n) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFFFF4500) else Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("${n}P", color = Color.White,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Player names
            Text(LocalizationManager.str("setup.playerName"),
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))
            playerNames.forEachIndexed { i, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(playerColors[i]))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { onNameChange(i, it) },
                        placeholder = { Text("Player ${i + 1}", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Red ball count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(LocalizationManager.str("setup.redBalls"), color = Color(0xFFDC143C),
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(16.dp))
                // Minus button
                IconButton(
                    onClick = { if (redBallCount > 1) onRedBallCountChange(redBallCount - 1) },
                    enabled = redBallCount > 1
                ) {
                    Text("−", color = Color.White, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Clickable number
                var showEditor by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { showEditor = true },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("$redBallCount", color = Color(0xFFDC143C),
                        fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
                if (showEditor) {
                    var editorValue by remember { mutableStateOf(redBallCount.toString()) }
                    AlertDialog(
                        onDismissRequest = { showEditor = false },
                        title = { Text(LocalizationManager.str("redEditor.title"), color = Color.White) },
                        text = {
                            OutlinedTextField(
                                value = editorValue,
                                onValueChange = { v ->
                                    val filtered = v.filter { it.isDigit() }
                                    if (filtered.length <= 2) editorValue = filtered
                                },
                                label = { Text(LocalizationManager.str("setup.redBalls"), color = Color.White.copy(alpha = 0.5f)) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFDC143C),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val n = editorValue.toIntOrNull()?.coerceIn(1, 15) ?: 1
                                onRedBallCountChange(n)
                                showEditor = false
                            }) { Text(LocalizationManager.str("redEditor.ok"), color = Color(0xFFDC143C)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditor = false }) {
                                Text(LocalizationManager.str("redEditor.cancel"), color = Color.White.copy(alpha = 0.6f))
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Plus button
                IconButton(
                    onClick = { if (redBallCount < 15) onRedBallCountChange(redBallCount + 1) },
                    enabled = redBallCount < 15
                ) {
                    Text("+", color = Color.White, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            // Start button
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4500)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(LocalizationManager.str("setup.startMatch"), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
