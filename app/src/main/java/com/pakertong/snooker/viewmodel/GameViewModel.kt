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
    private var initialRedCount = 15
    private var redOffset = 0
    var isColorPhase by mutableStateOf(false)
        private set
    var isFreeBallActive by mutableStateOf(false)
        private set
    var freeBallToggle by mutableStateOf(false)
    var endGamePhase by mutableIntStateOf(-1)
        private set
    var isReSpotBlack by mutableStateOf(false)
        private set
    var showReSpotDialog by mutableStateOf(false)
        private set
    var matchOver by mutableStateOf(false)
        private set
    private val endgameColorsPotted = mutableSetOf<String>()
    private val endgameOrder = listOf(
        SnookerBall.YELLOW, SnookerBall.GREEN, SnookerBall.BROWN,
        SnookerBall.BLUE, SnookerBall.PINK, SnookerBall.BLACK
    )
    private val endgameColorValues = mapOf(
        "ball.yellow" to 2, "ball.green" to 3, "ball.brown" to 4,
        "ball.blue" to 5, "ball.pink" to 6, "ball.black" to 7
    )

    val currentEndgameBall: SnookerBall?
        get() = if (endGamePhase in 0..<endgameOrder.size) endgameOrder[endGamePhase] else null
    
    val isScoresTied: Boolean
        get() {
            if (players.size < 2) return false
            val topScore = players.maxOf { it.score }
            return players.count { it.score == topScore } >= 2
        }
    
    val isEndgameComplete: Boolean
        get() = endGamePhase >= endgameOrder.size
    
    fun checkMatchEnd() {
        if (tableRemaining > 0) return
        // Table remaining is 0 → all balls potted
        if (isScoresTied && !isReSpotBlack) {
            showReSpotDialog = true  // Tied → re-spot black
        } else if (!isReSpotBlack) {
            matchOver = true  // Not tied → match over
        }
    }
    
    fun startReSpotBlack(selectedPlayerIdx: Int) {
        currentPlayerIdx = selectedPlayerIdx
        isReSpotBlack = true
        showReSpotDialog = false
    }

    // Events & undo
    val events = mutableStateListOf<ScoreEvent>()
    private val undoStack = mutableListOf<UndoAction>()

    val hasUndo get() = undoStack.isNotEmpty()
    val isGameActive get() = players.any { it.score > 0 }
    val sortedPlayers get() = players.sortedByDescending { it.score }

    val tableRemaining: Int
        get() {
            val colorValues = endgameColorValues
            var redsPotted = 0
            for (event in events) {
                if (!event.isFoul && event.ballLocKey == "ball.red") {
                    redsPotted++
                }
            }
            val redsLeft = maxOf(0, initialRedCount - redsPotted - redOffset)
            if (redsLeft > 0) {
                return redsLeft * 8 + 27  // reds × (1+7) + all colors = 27
            }
            // Endgame: subtract potted colors from 27
            val pottedSum = endgameColorsPotted.sumOf { colorValues[it] ?: 0 }
            return maxOf(0, 27 - pottedSum)
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
        resetGame()
        redsRemaining = redCount
        initialRedCount = redCount
    }

    fun resetGame() {
        for (i in players.indices) {
            players[i] = players[i].copy(score = 0, currentBreak = 0, highestBreak = 0)
        }
        events.clear()
        undoStack.clear()
        currentPlayerIdx = 0
        redsRemaining = 15
        endGamePhase = -1
        endgameColorsPotted.clear()
        isReSpotBlack = false
        redOffset = 0
        showReSpotDialog = false
        matchOver = false
        isColorPhase = false
        isFreeBallActive = false
        freeBallToggle = false
        matchStartTime = System.currentTimeMillis()
    }

    fun score(ball: SnookerBall) {
        val snap = captureSnapshot()
        val wasFreeBall = isFreeBallActive
        isFreeBallActive = false

        // Re-spot black: add score only, no break tracking, then end match
        if (isReSpotBlack && ball == SnookerBall.BLACK) {
            val p = players[currentPlayerIdx]
            players[currentPlayerIdx] = p.copy(score = p.score + SnookerBall.BLACK.points)
            events.add(ScoreEvent(
                playerName = p.name,
                points = SnookerBall.BLACK.points,
                ballLocKey = "ball.black"
            ))
            undoStack.add(UndoAction.Score(currentPlayerIdx, SnookerBall.BLACK.points, 0, snap))
            isReSpotBlack = false
            matchOver = true
            return
        }

        // Free ball: score as target ball value, not actual ball value
        val targetValue = if (wasFreeBall) {
            if (isColorPhase) 4 else 1  // color target=4, red target=1
        } else {
            ball.points
        }

        val p = players[currentPlayerIdx]
        val newScore = p.score + targetValue
        val newBreak = p.currentBreak + targetValue
        val newHighest = maxOf(p.highestBreak, newBreak)
        players[currentPlayerIdx] = p.copy(score = newScore, currentBreak = newBreak, highestBreak = newHighest)

        var redsChange = 0
        // Free ball pot doesn't consume a red
        if (!wasFreeBall && ball == SnookerBall.RED && redsRemaining > 0) {
            redsRemaining--
            redsChange = -1
        }

        // Endgame logic
        if (ball != SnookerBall.RED && redsRemaining == 0 && !wasFreeBall) {
            if (endGamePhase == -1) {
                endGamePhase = 0  // Lock to yellow sequence, first color doesn't subtract
            } else {
                // Only subtract potted colors when in active endgame sequence
                endgameColorsPotted.add("ball.${ball.name.lowercase()}")
                // Advance if it's the current target
                if (currentEndgameBall != null && ball == currentEndgameBall) {
                    endGamePhase++
                }
            }
        }

        // Free ball acts as red shot: next target is a color
        isColorPhase = (ball == SnookerBall.RED) || wasFreeBall

        events.add(ScoreEvent(
            playerName = players[currentPlayerIdx].name,
            points = targetValue,
            ballLocKey = if (wasFreeBall) "ball.freeball" else "ball.${ball.name.lowercase()}"
        ))
        undoStack.add(UndoAction.Score(currentPlayerIdx, targetValue, redsChange, snap))
        
        // Check re-spot condition after scoring
        checkMatchEnd()
    }

    fun scoreMultipleReds(count: Int) {
        val snap = captureSnapshot()
        isFreeBallActive = false
        isColorPhase = true

        val totalPoints = count * SnookerBall.RED.points
        val p = players[currentPlayerIdx]
        val newScore = p.score + totalPoints
        val newBreak = p.currentBreak + totalPoints
        val newHighest = maxOf(p.highestBreak, newBreak)
        players[currentPlayerIdx] = p.copy(score = newScore, currentBreak = newBreak, highestBreak = newHighest)

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
        checkMatchEnd()
    }

    fun missShot() {
        // Reset current break and pass turn, no points change
        players[currentPlayerIdx] = players[currentPlayerIdx].copy(currentBreak = 0)
        isFreeBallActive = false
        // Miss resets to red phase for next player
        isColorPhase = false
        // First miss after reds done: enter endgame with yellow
        if (redsRemaining == 0 && endGamePhase == -1) {
            endGamePhase = 0
        }
        nextTurn()
        checkMatchEnd()
    }

    fun foul(points: Int, redDeduction: Int = 0, activeFreeBall: Boolean = false) {
        // Capture state before foul (includes free ball for undo)
        val snap = captureSnapshot()
        
        // Any foul during free ball ends the free ball advantage
        isFreeBallActive = false
        // Foul resets to red phase for next player
        isColorPhase = false

        // Foul after reds done: enter endgame with yellow
        if (redsRemaining == 0 && endGamePhase == -1) {
            endGamePhase = 0
        }

        players[currentPlayerIdx] = players[currentPlayerIdx].copy(currentBreak = 0)

        // Add penalty points to all other players (iOS style, no deduction from fouling player)
        for (i in players.indices) {
            if (i != currentPlayerIdx) {
                players[i] = players[i].copy(score = players[i].score + points)
            }
        }

        var redsChange = 0
        val actualDeduction = redDeduction.coerceIn(0, redsRemaining)
        if (actualDeduction > 0) {
            redsRemaining -= actualDeduction
            redOffset += actualDeduction
            redsChange = -actualDeduction
        }

        events.add(ScoreEvent(
            playerName = players[currentPlayerIdx].name,
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
                players[action.playerIdx] = players[action.playerIdx].copy(
                    score = players[action.playerIdx].score - action.points,
                    currentBreak = (players[action.playerIdx].currentBreak - action.points).coerceAtLeast(0)
                )
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
                    if (i != action.playerIdx) players[i] = players[i].copy(
                        score = players[i].score - action.points
                    )
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
        players[currentPlayerIdx] = players[currentPlayerIdx].copy(currentBreak = 0)
        currentPlayerIdx = (currentPlayerIdx + 1) % players.size
    }

    private fun captureSnapshot() = EndGameSnapshot(
        phase = endGamePhase,
        isColorPhase = isColorPhase,
        playerIndex = currentPlayerIdx,
        freeBallActive = isFreeBallActive,
        colorsPotted = endgameColorsPotted.toSet(),
        redOffset = redOffset
    )

    private fun restoreSnapshot(snap: EndGameSnapshot) {
        endGamePhase = snap.phase
        currentPlayerIdx = snap.playerIndex
        isFreeBallActive = snap.freeBallActive
        endgameColorsPotted.clear()
        endgameColorsPotted.addAll(snap.colorsPotted)
        redOffset = snap.redOffset
        // Recalculate isColorPhase from events after undo
        isColorPhase = calculateColorPhase()
    }

    private fun calculateColorPhase(): Boolean {
        if (redsRemaining == 0) return false
        // Look at last non-foul event to determine phase
        for (i in events.indices.reversed()) {
            val e = events[i]
            if (!e.isFoul) {
                return e.ballLocKey == "ball.red" || e.ballLocKey == "ball.freeball"
            }
        }
        return false  // No events: red phase
    }

    fun isBallDisabled(ball: SnookerBall): Boolean {
        // Re-spot black: only black is active
        if (isReSpotBlack) return ball != SnookerBall.BLACK
        // Free ball: all balls active
        if (isFreeBallActive) return false
        // Endgame: only current target color is active
        if (redsRemaining == 0 && endGamePhase >= 0) {
            return ball == SnookerBall.RED || ball != currentEndgameBall
        }
        if (redsRemaining == 0 && endGamePhase == -1) {
            return ball == SnookerBall.RED
        }
        return if (ball == SnookerBall.RED) isColorPhase else !isColorPhase
    }
}
