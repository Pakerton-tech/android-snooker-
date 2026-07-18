package com.pakertong.snooker.model

// Ball values
enum class SnookerBall(val points: Int, val hexColor: Long) {
    RED(1, 0xFFDC143C),
    YELLOW(2, 0xFFFFD700),
    GREEN(3, 0xFF228B22),
    BROWN(4, 0xFF8B4513),
    BLUE(5, 0xFF1E90FF),
    PINK(6, 0xFFFF69B4),
    BLACK(7, 0xFF333333)
}

data class Player(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    var score: Int = 0,
    var currentBreak: Int = 0,
    var highestBreak: Int = 0,
    val colorIndex: Int
) {
    companion object {
        val playerColors = listOf(0xFFFF4500, 0xFF1E90FF, 0xFF32CD32, 0xFFFFD700)
    }
    val color: Long get() = playerColors[colorIndex % playerColors.size]
}

data class ScoreEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val playerName: String,
    val points: Int,
    val ballLocKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFoul: Boolean = false
)

// Undo support
data class EndGameSnapshot(
    val phase: Int = -1,
    val isColorPhase: Boolean = false,
    val playerIndex: Int = 0,
    val freeBallActive: Boolean = false,
    val redOffset: Int = 0,
    val colorsPotted: Set<String> = emptySet()
)

sealed class UndoAction {
    data class Score(
        val playerIdx: Int,
        val points: Int,
        val redsChange: Int = 0,
        val snapshot: EndGameSnapshot
    ) : UndoAction()

    data class Foul(
        val playerIdx: Int,
        val points: Int,
        val redsChange: Int = 0,
        val snapshot: EndGameSnapshot
    ) : UndoAction()

    data class Miss(val snapshot: EndGameSnapshot) : UndoAction()
}

// Match Record
data class MatchRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val duration: Long = 0,
    val players: List<PlayerSnapshot> = emptyList(),
    val winnerName: String = "",
    val events: List<ScoreEvent> = emptyList(),
    val notes: String = ""
) {
    val bestBreak: Int get() = players.maxOfOrNull { it.highestBreak } ?: 0
}

data class PlayerSnapshot(
    val name: String,
    val score: Int,
    val highestBreak: Int,
    val colorIndex: Int
)
