package com.pakertong.snooker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pakertong.snooker.viewmodel.GameViewModel

@Composable
fun MainScreen(vm: GameViewModel = viewModel()) {
    var inGame by remember { mutableStateOf(false) }
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var playerCount by remember { mutableIntStateOf(2) }
    var redBallCount by remember { mutableIntStateOf(15) }

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
            onEndMatch = { inGame = false }
        )
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text("Snooker Scorekeeper",
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Register players & start match",
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(32.dp))

            // Player count
            Text("Number of Players",
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
            Text("Player Names",
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
                Text("Red balls", color = Color(0xFFDC143C),
                    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Text("$redBallCount", color = Color(0xFFDC143C),
                    fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    IconButton(onClick = { if (redBallCount > 1) onRedBallCountChange(redBallCount - 1) }) {
                        Text("−", color = Color.White, fontSize = 20.sp)
                    }
                    IconButton(onClick = { if (redBallCount < 30) onRedBallCountChange(redBallCount + 1) }) {
                        Text("+", color = Color.White, fontSize = 20.sp)
                    }
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
                Text("Start Match", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
