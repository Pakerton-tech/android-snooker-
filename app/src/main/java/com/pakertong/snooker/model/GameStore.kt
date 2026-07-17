package com.pakertong.snooker.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class GameStore(private val context: Context) {
    private val matches = mutableListOf<MatchRecord>()
    val allMatches: List<MatchRecord> get() = matches.toList()

    private val file: File get() = File(context.filesDir, "match_history.json")

    fun load() {
        if (!file.exists()) return
        try {
            val text = file.readText()
            val arr = JSONArray(text)
            matches.clear()
            for (i in 0 until arr.length()) {
                matches.add(parseMatch(arr.getJSONObject(i)))
            }
        } catch (e: Exception) {
            matches.clear()
        }
    }

    fun save(match: MatchRecord) {
        matches.add(0, match)
        persist()
    }

    fun delete(matchId: String) {
        matches.removeAll { it.id == matchId }
        persist()
    }

    fun deleteAll() {
        matches.clear()
        persist()
    }

    val count: Int get() = matches.size

    private fun persist() {
        val arr = JSONArray()
        matches.forEach { arr.put(toJson(it)) }
        file.writeText(arr.toString(2))
    }

    private fun toJson(m: MatchRecord): JSONObject = JSONObject().apply {
        put("id", m.id)
        put("date", m.date)
        put("duration", m.duration)
        put("winnerName", m.winnerName)
        put("notes", m.notes)
        val playersArr = JSONArray()
        m.players.forEach { p ->
            playersArr.put(JSONObject().apply {
                put("name", p.name)
                put("score", p.score)
                put("highestBreak", p.highestBreak)
                put("colorIndex", p.colorIndex)
            })
        }
        put("players", playersArr)
        val eventsArr = JSONArray()
        m.events.forEach { e ->
            eventsArr.put(JSONObject().apply {
                put("playerName", e.playerName)
                put("points", e.points)
                put("ballLocKey", e.ballLocKey)
                put("timestamp", e.timestamp)
                put("isFoul", e.isFoul)
            })
        }
        put("events", eventsArr)
    }

    private fun parseMatch(obj: JSONObject): MatchRecord {
        val players = mutableListOf<PlayerSnapshot>()
        val pArr = obj.optJSONArray("players")
        if (pArr != null) {
            for (i in 0 until pArr.length()) {
                val p = pArr.getJSONObject(i)
                players.add(PlayerSnapshot(
                    name = p.getString("name"),
                    score = p.getInt("score"),
                    highestBreak = p.getInt("highestBreak"),
                    colorIndex = p.getInt("colorIndex")
                ))
            }
        }
        val events = mutableListOf<ScoreEvent>()
        val eArr = obj.optJSONArray("events")
        if (eArr != null) {
            for (i in 0 until eArr.length()) {
                val e = eArr.getJSONObject(i)
                events.add(ScoreEvent(
                    playerName = e.getString("playerName"),
                    points = e.getInt("points"),
                    ballLocKey = e.getString("ballLocKey"),
                    timestamp = e.getLong("timestamp"),
                    isFoul = e.optBoolean("isFoul", false)
                ))
            }
        }
        return MatchRecord(
            id = obj.getString("id"),
            date = obj.getLong("date"),
            duration = obj.getLong("duration"),
            players = players,
            winnerName = obj.getString("winnerName"),
            events = events,
            notes = obj.optString("notes", "")
        )
    }
}
