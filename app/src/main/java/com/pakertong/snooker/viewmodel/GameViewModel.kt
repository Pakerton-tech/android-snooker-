package com.pakertong.snooker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pakertong.snooker.model.*

class GameViewModel : ViewModel() {
    // Players
    var players = mutableStateListOf<Player>()
        private set
    var currentPlayerIdx by mutableIntStateOf(0)
        private set
    val currentPlayer get() = players.getOrElse(currentPlayerIdx) { players.first() }

    // Game state
    var redsRemaining by mutableIntStateOf(15)
        private set
    var isColorPhase by mutableStateOf(false)
        private set
    var isFreeBallActive by mutableStateOf(false)
        private set
    var freeBallToggle by mutableStateOf(false)

    // Events & undo
    val events = mutableStateListOf<ScoreEvent>()
    private val undoStack = mutableListOf<UndoAction>()

    val hasUndo get() = undoStack.isNotEmpty()
    val isGameActive get() = players.any { it.score > 0 }
    val sortedPlayers get() = players.sortedByDescending { it.score }

    val tableRemaining: Int
        get() {
            if (redsRemaining <= 0) return 27
            return redsRemaining * 8 + 27
        }

    val redsDone get() = redsRemaining == 0

    // Match timer
    var matchStartTime by mutableStateOf(System.currentTimeMillis())
        private set

    // Tab tracking
    var currentTab by mutableIntStateOf(0)

    fun setupPlayers(names: List<String>, redCount: Int) {
        players.clear()
        names.forEachIndexed { i, name ->
            players.add(Player(
                name = name.ifBlank { "Player ${i + 1}" },
                colorIndex = i
            ))
        }
        redsRemaining = redCount
        resetGame()
    }

    fun resetGame() {
        for (p in players) {
            p.score = 0
            p.currentBreak = 0
            p.highestBreak = 0
        }
        events.clear()
        undoStack.clear()
        currentPlayerIdx = 0
        redsRemaining = 15
        isColorPhase = false
        isFreeBallActive = false
        freeBallToggle = false
        matchStartTime = System.currentTimeMillis()
    }

    fun score(ball: SnookerBall) {
        val snap = captureSnapshot()
        isFreeBallActive = false

        val p = players[currentPlayerIdx]
        p.score += ball.points
        p.currentBreak += ball.points
        if (p.currentBreak > p.highestBreak) p.highestBreak = p.currentBreak

        var redsChange = 0
        if (ball == SnookerBall.RED && redsRemaining > 0) {
            redsRemaining--
            redsChange = -1
        }

        isColorPhase = ball == SnookerBall.RED

        events.add(ScoreEvent(
            playerName = p.name,
            points = ball.points,
            ballLocKey = "ball.${ball.name.lowercase()}"
        ))
        undoStack.add(UndoAction.Score(currentPlayerIdx, ball.points, redsChange, snap))
        // Player continues after scoring (no turn change)
    }

    fun scoreMultipleReds(count: Int) {
        val snap = captureSnapshot()
        isFreeBallActive = false
        isColorPhase = true

        val totalPoints = count * SnookerBall.RED.points
        val p = players[currentPlayerIdx]
        p.score += totalPoints
        p.currentBreak += totalPoints
        if (p.currentBreak > p.highestBreak) p.highestBreak = p.currentBreak

        repeat(count) {
            redsRemaining--
            events.add(ScoreEvent(
                playerName = p.name,
                points = SnookerBall.RED.points,
                ballLocKey = "ball.red"
            ))
        }
        undoStack.add(UndoAction.Score(currentPlayerIdx, totalPoints, -count, snap))
        // Player continues after scoring (no turn change)
    }

    fun missShot() {
        // Reset current break and pass turn, no points change
        val p = players[currentPlayerIdx]
        p.currentBreak = 0
        isFreeBallActive = false
        nextTurn()
    }

    fun foul(points: Int, redDeduction: Int = 0, activeFreeBall: Boolean = false) {
        // Capture state before foul (includes free ball for undo)
        val snap = captureSnapshot()
        
        // Any foul during free ball ends the free ball advantage
        isFreeBallActive = false

        val p = players[currentPlayerIdx]
        p.currentBreak = 0

        // Add penalty points to all other players (iOS style, no deduction from fouling player)
        for (i in players.indices) {
            if (i != currentPlayerIdx) players[i].score += points
        }

        var redsChange = 0
        val actualDeduction = redDeduction.coerceIn(0, redsRemaining)
        if (actualDeduction > 0) {
            redsRemaining -= actualDeduction
            redsChange = -actualDeduction
        }

        events.add(ScoreEvent(
            playerName = p.name,
            points = points,
            ballLocKey = "ball.foul",
            isFoul = true
        ))
        undoStack.add(UndoAction.Foul(currentPlayerIdx, points, redsChange, snap))
        
        nextTurn()
        
        // Activate free ball after turn change if toggle was on
        if (activeFreeBall) {
            isFreeBallActive = true
            freeBallToggle = false
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val action = undoStack.removeLast()
        var redsChange = 0
        when (action) {
            is UndoAction.Score -> {
                players[action.playerIdx].score -= action.points
                players[action.playerIdx].currentBreak =
                    (players[action.playerIdx].currentBreak - action.points).coerceAtLeast(0)
                events.removeLast()
                if (action.redsChange < 0) {
                    repeat((-action.redsChange) - 1) { events.removeLast() }
                }
                currentPlayerIdx = action.playerIdx
                restoreSnapshot(action.snapshot)
                redsChange = action.redsChange
            }
            is UndoAction.Foul -> {
                // Undo: subtract foul points from all other players (iOS style)
                for (i in players.indices) {
                    if (i != action.playerIdx) players[i].score -= action.points
                }
                events.removeLast()
                currentPlayerIdx = action.playerIdx
                restoreSnapshot(action.snapshot)
                redsChange = action.redsChange
            }
        }
        if (redsChange != 0) {
            redsRemaining -= redsChange
        }
    }

    private fun nextTurn() {
        players[currentPlayerIdx].currentBreak = 0
        currentPlayerIdx = (currentPlayerIdx + 1) % players.size
    }

    private fun captureSnapshot() = EndGameSnapshot(
        isColorPhase = isColorPhase,
        playerIndex = currentPlayerIdx,
        freeBallActive = isFreeBallActive
    )

    private fun restoreSnapshot(snap: EndGameSnapshot) {
        isColorPhase = snap.isColorPhase
        currentPlayerIdx = snap.playerIndex
        isFreeBallActive = snap.freeBallActive
    }

    fun isBallDisabled(ball: SnookerBall): Boolean {
        // All balls enabled during normal play
        return false
    }
}
