package com.pakertong.snooker.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    ZH_CN("zh-CN"),
    JA_JP("ja-JP"),
    EN("en");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}

object LocalizationManager {
    var currentLanguage by mutableStateOf(AppLanguage.SYSTEM)

    fun str(key: String): String {
        val lang = if (currentLanguage == AppLanguage.SYSTEM) detectSystemLanguage() else currentLanguage
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.EN) ?: key
    }

    fun strf(key: String, vararg args: Any): String {
        return String.format(str(key), *args)
    }

    private fun detectSystemLanguage(): AppLanguage {
        val locale = java.util.Locale.getDefault()
        return when {
            locale.language == "zh" -> AppLanguage.ZH_CN
            locale.language == "ja" -> AppLanguage.JA_JP
            else -> AppLanguage.EN
        }
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        // App
        "app.name" to mapOf(
            AppLanguage.ZH_CN to "斯诺克计分器",
            AppLanguage.JA_JP to "スヌーカー採点",
            AppLanguage.EN to "Snooker Scorekeeper"
        ),
        "app.subtitle" to mapOf(
            AppLanguage.ZH_CN to "登记球员，开始比赛",
            AppLanguage.JA_JP to "プレイヤーを登録して試合開始",
            AppLanguage.EN to "Register players & start match"
        ),
        // Setup
        "setup.playerCount" to mapOf(
            AppLanguage.ZH_CN to "玩家人数",
            AppLanguage.JA_JP to "プレイヤー人数",
            AppLanguage.EN to "Number of Players"
        ),
        "setup.playerName" to mapOf(
            AppLanguage.ZH_CN to "球员姓名",
            AppLanguage.JA_JP to "プレイヤー名",
            AppLanguage.EN to "Player Names"
        ),
        "setup.player" to mapOf(
            AppLanguage.ZH_CN to "球员",
            AppLanguage.JA_JP to "プレイヤー",
            AppLanguage.EN to "Player"
        ),
        "setup.startMatch" to mapOf(
            AppLanguage.ZH_CN to "开始比赛",
            AppLanguage.JA_JP to "試合開始",
            AppLanguage.EN to "Start Match"
        ),
        "setup.redBalls" to mapOf(
            AppLanguage.ZH_CN to "红球数",
            AppLanguage.JA_JP to "赤ボール数",
            AppLanguage.EN to "Red Balls"
        ),
        // Scoreboard
        "sb.undo" to mapOf(
            AppLanguage.ZH_CN to "撤销",
            AppLanguage.JA_JP to "戻す",
            AppLanguage.EN to "Undo"
        ),
        "sb.foul" to mapOf(
            AppLanguage.ZH_CN to "犯规・特殊处理" +
                    "",
            AppLanguage.JA_JP to "ファウル・特別処理",
            AppLanguage.EN to "Foul・Special"
        ),
        "sb.end" to mapOf(
            AppLanguage.ZH_CN to "结束",
            AppLanguage.JA_JP to "終了",
            AppLanguage.EN to "End"
        ),
        "sb.miss" to mapOf(
            AppLanguage.ZH_CN to "未进球",
            AppLanguage.JA_JP to "ハズレ",
            AppLanguage.EN to "Miss"
        ),
        "sb.shooting" to mapOf(
            AppLanguage.ZH_CN to "击球",
            AppLanguage.JA_JP to "の番",
            AppLanguage.EN to "'s turn"
        ),
        "sb.reds" to mapOf(
            AppLanguage.ZH_CN to "剩余红球",
            AppLanguage.JA_JP to "残り赤ボール",
            AppLanguage.EN to "Reds"
        ),
        "sb.table" to mapOf(
            AppLanguage.ZH_CN to "剩余分数",
            AppLanguage.JA_JP to "残り点数",
            AppLanguage.EN to "Remaining"
        ),
        "sb.behind" to mapOf(
            AppLanguage.ZH_CN to "落后",
            AppLanguage.JA_JP to "遅れ",
            AppLanguage.EN to "Behind"
        ),
        "sb.behindPts" to mapOf(
            AppLanguage.ZH_CN to "分",
            AppLanguage.JA_JP to "点",
            AppLanguage.EN to " pts"
        ),
        "sb.maxBreak" to mapOf(
            AppLanguage.ZH_CN to "MAXIMUM BREAK!",
            AppLanguage.JA_JP to "MAXIMUM BREAK!",
            AppLanguage.EN to "MAXIMUM BREAK!"
        ),
        "sb.break" to mapOf(
            AppLanguage.ZH_CN to "单杆",
            AppLanguage.JA_JP to "連続得点",
            AppLanguage.EN to "Break"
        ),
        "sb.highestBreak" to mapOf(
            AppLanguage.ZH_CN to "最高单杆",
            AppLanguage.JA_JP to "最高連続得点",
            AppLanguage.EN to "Highest Break"
        ),
        "sb.freeBall" to mapOf(
            AppLanguage.ZH_CN to "自由球!",
            AppLanguage.JA_JP to "フリーボール!",
            AppLanguage.EN to "Free Ball!"
        ),
        "sb.endTitle" to mapOf(
            AppLanguage.ZH_CN to "结束比赛？",
            AppLanguage.JA_JP to "試合を終了しますか？",
            AppLanguage.EN to "End Match?"
        ),
        "sb.endMsg" to mapOf(
            AppLanguage.ZH_CN to "比赛记录将保存到历史中",
            AppLanguage.JA_JP to "試合記録は履歴に保存されます",
            AppLanguage.EN to "Saved to history"
        ),
        "sb.saveEnd" to mapOf(
            AppLanguage.ZH_CN to "保存并结束",
            AppLanguage.JA_JP to "保存して終了",
            AppLanguage.EN to "Save & End"
        ),
        "sb.continue" to mapOf(
            AppLanguage.ZH_CN to "继续比赛",
            AppLanguage.JA_JP to "続ける",
            AppLanguage.EN to "Continue"
        ),
        // Ball names
        "ball.red" to mapOf(
            AppLanguage.ZH_CN to "红球",
            AppLanguage.JA_JP to "レッド",
            AppLanguage.EN to "Red"
        ),
        "ball.yellow" to mapOf(
            AppLanguage.ZH_CN to "黄球",
            AppLanguage.JA_JP to "イエロー",
            AppLanguage.EN to "Yellow"
        ),
        "ball.green" to mapOf(
            AppLanguage.ZH_CN to "绿球",
            AppLanguage.JA_JP to "グリーン",
            AppLanguage.EN to "Green"
        ),
        "ball.brown" to mapOf(
            AppLanguage.ZH_CN to "棕球",
            AppLanguage.JA_JP to "ブラウン",
            AppLanguage.EN to "Brown"
        ),
        "ball.blue" to mapOf(
            AppLanguage.ZH_CN to "蓝球",
            AppLanguage.JA_JP to "ブルー",
            AppLanguage.EN to "Blue"
        ),
        "ball.pink" to mapOf(
            AppLanguage.ZH_CN to "粉球",
            AppLanguage.JA_JP to "ピンク",
            AppLanguage.EN to "Pink"
        ),
        "ball.black" to mapOf(
            AppLanguage.ZH_CN to "黑球",
            AppLanguage.JA_JP to "ブラック",
            AppLanguage.EN to "Black"
        ),
        "ball.foul" to mapOf(
            AppLanguage.ZH_CN to "犯规",
            AppLanguage.JA_JP to "ファウル",
            AppLanguage.EN to "Foul"
        ),
        // Foul Sheet
        "foul.title" to mapOf(
            AppLanguage.ZH_CN to "犯规罚分",
            AppLanguage.JA_JP to "ファウル罰点",
            AppLanguage.EN to "Foul Penalty"
        ),
        "foul.tab" to mapOf(
            AppLanguage.ZH_CN to "犯规罚分",
            AppLanguage.JA_JP to "ファウル罰点",
            AppLanguage.EN to "Foul Penalty"
        ),
        "foul.special" to mapOf(
            AppLanguage.ZH_CN to "特殊处理",
            AppLanguage.JA_JP to "特別処理",
            AppLanguage.EN to "Special"
        ),
        "foul.reduceRed" to mapOf(
            AppLanguage.ZH_CN to "红球减少",
            AppLanguage.JA_JP to "赤ボール減少",
            AppLanguage.EN to "Reduce Red"
        ),
        "foul.custom" to mapOf(
            AppLanguage.ZH_CN to "自定义罚分",
            AppLanguage.JA_JP to "カスタム罰点",
            AppLanguage.EN to "Custom Penalty"
        ),
        "foul.confirm" to mapOf(
            AppLanguage.ZH_CN to "确认罚 %d 分",
            AppLanguage.JA_JP to "%d点で確定",
            AppLanguage.EN to "Confirm %d pts"
        ),
        "foul.freeBall" to mapOf(
            AppLanguage.ZH_CN to "自由球",
            AppLanguage.JA_JP to "フリーボール",
            AppLanguage.EN to "Free Ball"
        ),
        "foul.freeBallDesc" to mapOf(
            AppLanguage.ZH_CN to "对方犯规后，指定任意球作为目标球击打",
            AppLanguage.JA_JP to "相手の反則後、任意のボールを目標として撞く",
            AppLanguage.EN to "Opponent fouled. Nominate any ball as target."
        ),
        "foul.minFoul" to mapOf(
            AppLanguage.ZH_CN to "最少罚 4 分",
            AppLanguage.JA_JP to "最低4点",
            AppLanguage.EN to "Min 4 pts"
        ),
        "foul.snooker" to mapOf(
            AppLanguage.ZH_CN to "让杆",
            AppLanguage.JA_JP to "パス",
            AppLanguage.EN to "Pass to opponent"
        ),
        "foul.snookerDesc" to mapOf(
            AppLanguage.ZH_CN to "让对手击球",
            AppLanguage.JA_JP to "相手にパス",
            AppLanguage.EN to "Pass turn to next player"
        ),
        // Red count menu
        "redCount.title" to mapOf(
            AppLanguage.ZH_CN to "红球 × ?",
            AppLanguage.JA_JP to "赤ボール × ?",
            AppLanguage.EN to "Red Ball x ?"
        ),
        "redCount.cancel" to mapOf(
            AppLanguage.ZH_CN to "取消",
            AppLanguage.JA_JP to "キャンセル",
            AppLanguage.EN to "Cancel"
        ),
        "redCount.reds" to mapOf(
            AppLanguage.ZH_CN to "%d 红 (%d分)",
            AppLanguage.JA_JP to "%d 赤 (%d点)",
            AppLanguage.EN to "%d Reds (%dpts)"
        ),
        // Re-spot
        "respot.title" to mapOf(
            AppLanguage.ZH_CN to "争黑!",
            AppLanguage.JA_JP to "ブラックボールゲーム!",
            AppLanguage.EN to "Black Ball Game!"
        ),
        "respot.msg" to mapOf(
            AppLanguage.ZH_CN to "双方得分相等，请选择开球球员",
            AppLanguage.JA_JP to "同点です。先攻を選んでください",
            AppLanguage.EN to "Scores are tied. Who will start?"
        ),
        // Red editor
        "redEditor.title" to mapOf(
            AppLanguage.ZH_CN to "红球数",
            AppLanguage.JA_JP to "赤球数",
            AppLanguage.EN to "Red Balls"
        ),
        "redEditor.ok" to mapOf(
            AppLanguage.ZH_CN to "确定",
            AppLanguage.JA_JP to "OK",
            AppLanguage.EN to "OK"
        ),
        "redEditor.cancel" to mapOf(
            AppLanguage.ZH_CN to "取消",
            AppLanguage.JA_JP to "キャンセル",
            AppLanguage.EN to "Cancel"
        ),

        // History
        "history.title" to mapOf(
            AppLanguage.ZH_CN to "比赛历史",
            AppLanguage.JA_JP to "試合履歴",
            AppLanguage.EN to "Match History"
        ),
        "history.empty" to mapOf(
            AppLanguage.ZH_CN to "还没有比赛记录",
            AppLanguage.JA_JP to "試合記録がありません",
            AppLanguage.EN to "No matches yet"
        ),
        "history.duration" to mapOf(
            AppLanguage.ZH_CN to "用时",
            AppLanguage.JA_JP to "試合時間",
            AppLanguage.EN to "Duration"
        ),
        "history.highestBreak" to mapOf(
            AppLanguage.ZH_CN to "最高单杆",
            AppLanguage.JA_JP to "最高連続得点",
            AppLanguage.EN to "Highest Break"
        ),
        "history.winner" to mapOf(
            AppLanguage.ZH_CN to "%s 获胜！",
            AppLanguage.JA_JP to "%s 勝利！",
            AppLanguage.EN to "%s wins!"
        ),
        "history.draw" to mapOf(
            AppLanguage.ZH_CN to "平局",
            AppLanguage.JA_JP to "引き分け",
            AppLanguage.EN to "Draw"
        ),
        "history.points" to mapOf(
            AppLanguage.ZH_CN to "%d 分",
            AppLanguage.JA_JP to "%d 点",
            AppLanguage.EN to "%d pts"
        ),
        // Detail
        "detail.close" to mapOf(
            AppLanguage.ZH_CN to "关闭",
            AppLanguage.JA_JP to "閉じる",
            AppLanguage.EN to "Close"
        ),
        "detail.delete" to mapOf(
            AppLanguage.ZH_CN to "删除",
            AppLanguage.JA_JP to "削除",
            AppLanguage.EN to "Delete"
        ),
        "detail.share" to mapOf(
            AppLanguage.ZH_CN to "分享",
            AppLanguage.JA_JP to "共有",
            AppLanguage.EN to "Share"
        ),
        "detail.deleteConfirm" to mapOf(
            AppLanguage.ZH_CN to "删除记录？",
            AppLanguage.JA_JP to "記録を削除しますか？",
            AppLanguage.EN to "Delete record?"
        ),
        "detail.deleteMsg" to mapOf(
            AppLanguage.ZH_CN to "此操作不可撤销",
            AppLanguage.JA_JP to "この操作は取り消せません",
            AppLanguage.EN to "This cannot be undone"
        ),
        "detail.deleteAll" to mapOf(
            AppLanguage.ZH_CN to "清空所有记录？",
            AppLanguage.JA_JP to "すべて削除しますか？",
            AppLanguage.EN to "Delete all records?"
        ),
        "detail.deleteAllMsg" to mapOf(
            AppLanguage.ZH_CN to "共 %d 场比赛记录将被永久删除",
            AppLanguage.JA_JP to "%d 件の記録が削除されます",
            AppLanguage.EN to "%d records will be deleted"
        ),
        "detail.cancel" to mapOf(
            AppLanguage.ZH_CN to "取消",
            AppLanguage.JA_JP to "キャンセル",
            AppLanguage.EN to "Cancel"
        ),

        // Settings
        "settings.title" to mapOf(
            AppLanguage.ZH_CN to "设置",
            AppLanguage.JA_JP to "設定",
            AppLanguage.EN to "Settings"
        ),
        "settings.language" to mapOf(
            AppLanguage.ZH_CN to "语言",
            AppLanguage.JA_JP to "言語",
            AppLanguage.EN to "Language"
        ),
        "settings.followSystem" to mapOf(
            AppLanguage.ZH_CN to "跟随系统",
            AppLanguage.JA_JP to "システムに従う",
            AppLanguage.EN to "Follow System"
        ),
        "settings.theme" to mapOf(
            AppLanguage.ZH_CN to "主题",
            AppLanguage.JA_JP to "テーマ",
            AppLanguage.EN to "Theme"
        ),
        "settings.dark" to mapOf(
            AppLanguage.ZH_CN to "深色模式",
            AppLanguage.JA_JP to "ダークモード",
            AppLanguage.EN to "Dark Mode"
        ),
        "settings.light" to mapOf(
            AppLanguage.ZH_CN to "浅色模式",
            AppLanguage.JA_JP to "ライトモード",
            AppLanguage.EN to "Light Mode"
        ),
        "settings.data" to mapOf(
            AppLanguage.ZH_CN to "数据管理",
            AppLanguage.JA_JP to "データ管理",
            AppLanguage.EN to "Data"
        ),
        "settings.clearAll" to mapOf(
            AppLanguage.ZH_CN to "清空全部记录",
            AppLanguage.JA_JP to "全履歴を削除",
            AppLanguage.EN to "Clear All Records"
        ),
        "settings.records" to mapOf(
            AppLanguage.ZH_CN to "共 %d 场比赛记录",
            AppLanguage.JA_JP to "全 %d 件の記録",
            AppLanguage.EN to "%d records"
        ),
        "settings.deleteAll" to mapOf(
            AppLanguage.ZH_CN to "清空所有记录？",
            AppLanguage.JA_JP to "すべて削除しますか？",
            AppLanguage.EN to "Delete all records?"
        ),
        "settings.deleteMsg" to mapOf(
            AppLanguage.ZH_CN to "共 %d 场比赛记录将被永久删除",
            AppLanguage.JA_JP to "%d 件の記録が削除されます",
            AppLanguage.EN to "%d records will be deleted"
        ),
        "settings.delete" to mapOf(
            AppLanguage.ZH_CN to "删除",
            AppLanguage.JA_JP to "削除",
            AppLanguage.EN to "Delete"
        ),
        // Bottom nav
        "nav.game" to mapOf(
            AppLanguage.ZH_CN to "比赛",
            AppLanguage.JA_JP to "試合",
            AppLanguage.EN to "Game"
        ),
        "nav.history" to mapOf(
            AppLanguage.ZH_CN to "历史",
            AppLanguage.JA_JP to "履歴",
            AppLanguage.EN to "History"
        ),
        "nav.settings" to mapOf(
            AppLanguage.ZH_CN to "设置",
            AppLanguage.JA_JP to "設定",
            AppLanguage.EN to "Settings"
        ),
        // Detail extra
        "detail.events" to mapOf(
            AppLanguage.ZH_CN to "事件记录",
            AppLanguage.JA_JP to "イベント記録",
            AppLanguage.EN to "Events"
        ),
        // History filter
        "history.showAll" to mapOf(
            AppLanguage.ZH_CN to "全部",
            AppLanguage.JA_JP to "すべて",
            AppLanguage.EN to "Show All"
        ),
        // Settings about
        "settings.version" to mapOf(
            AppLanguage.ZH_CN to "版本",
            AppLanguage.JA_JP to "バージョン",
            AppLanguage.EN to "Version"
        ),
        "settings.about" to mapOf(
            AppLanguage.ZH_CN to "关于",
            AppLanguage.JA_JP to "について",
            AppLanguage.EN to "About"
        ),
    )
}
