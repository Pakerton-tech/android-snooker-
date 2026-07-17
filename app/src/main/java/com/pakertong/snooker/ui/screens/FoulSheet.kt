package com.pakertong.snooker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pakertong.snooker.model.SnookerBall
import com.pakertong.snooker.model.LocalizationManager
import com.pakertong.snooker.viewmodel.GameViewModel

@Composable
fun FoulSheetDialog(vm: GameViewModel, onDismiss: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a2e)),
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Column {
                // Tab row
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color(0xFF0f0f23),
                    contentColor = Color.White
                ) {
                    Tab(selected = tab == 0, onClick = { tab = 0 },
                        text = { Text(LocalizationManager.str("foul.tab"), fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(selected = tab == 1, onClick = { tab = 1 },
                        text = { Text(LocalizationManager.str("foul.special"), fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                when (tab) {
                    0 -> FoulTab(vm = vm, onDismiss = onDismiss)
                    1 -> SpecialTab(vm = vm, onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
fun FoulTab(vm: GameViewModel, onDismiss: () -> Unit) {
    var redDeduction by remember { mutableFloatStateOf(0f) }
    var customFoul by remember { mutableFloatStateOf(4f) }
    var freeBallEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(LocalizationManager.str("foul.tab"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.12f))

        // Red ball deduction
        Text(LocalizationManager.str("foul.reduceRed"), color = Color(0xFFFF4500), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("0", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Slider(
                value = redDeduction,
                onValueChange = { redDeduction = it },
                valueRange = 0f..vm.redsRemaining.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF4500)),
                modifier = Modifier.weight(1f)
            )
            Text("${vm.redsRemaining}", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("${redDeduction.toInt()}", color = Color(0xFFFF4500), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.12f))

        // Custom penalty
        Text(LocalizationManager.str("foul.custom"), color = Color(0xFFFF9800), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("4", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Slider(
                value = customFoul,
                onValueChange = { customFoul = it },
                valueRange = 4f..20f,
                steps = 15,
                colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF9800)),
                modifier = Modifier.weight(1f)
            )
            Text("20", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("${customFoul.toInt()}", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { vm.foul(customFoul.toInt(), redDeduction.toInt(), freeBallEnabled); onDismiss() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()) {
            Text(LocalizationManager.strf("foul.confirm", customFoul.toInt()), color = Color.White)
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.12f))

        // Free ball toggle
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Color.Green.copy(alpha = 0.08f)).padding(12.dp)) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(LocalizationManager.str("foul.freeBall"), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(LocalizationManager.str("foul.freeBallDesc"),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            Switch(checked = freeBallEnabled, onCheckedChange = { freeBallEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Color.Green))
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.12f))

        // Ball penalties
        SnookerBall.entries.forEach { ball ->
            val penalty = maxOf(ball.points, 4)
            ListItem(
                headlineContent = { Text("${ball.name} ($penalty)", color = Color.White) },
                trailingContent = { Text("-$penalty", color = Color.Red, fontWeight = FontWeight.Bold) },
                leadingContent = { Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(ball.hexColor))) },
                modifier = Modifier.clickable { vm.foul(penalty, redDeduction.toInt(), freeBallEnabled); onDismiss() }
            )
        }
        ListItem(
            headlineContent = { Text(LocalizationManager.str("foul.minFoul"), color = Color.White) },
            trailingContent = { Text("-4", color = Color.Red, fontWeight = FontWeight.Bold) },
            modifier = Modifier.clickable { vm.foul(4, redDeduction.toInt(), freeBallEnabled); onDismiss() }
        )
    }
}

@Composable
fun SpecialTab(vm: GameViewModel, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Special", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = Color.White.copy(alpha = 0.12f))

        // 让杆 (Leave a snooker)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E90FF).copy(alpha = 0.1f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { vm.missShot(); onDismiss() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null,
                    tint = Color(0xFF1E90FF), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(LocalizationManager.str("foul.snooker"), color = Color.White,
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(LocalizationManager.str("foul.snookerDesc"),
                        color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Placeholder for future special options
        Text("More options coming soon...",
            color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
    }
}
