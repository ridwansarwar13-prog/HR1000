package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Real-time Hardware Performance Telemetry
 */
data class HardwareTelemetry(
    val cpuTemperature: Float = 38.5f,
    val gpuTemperature: Float = 39.0f,
    val batteryTemperature: Float = 34.0f,
    val fps: Double = 60.0,
    val frameTimeMs: Double = 16.6,
    val ramUsedMb: Long = 2800L,
    val ramTotalMb: Long = 6144L,
    val cpuUsagePercent: Int = 32,
    val thermalStatusName: String = "NORMAL",
    val isThrottling: Boolean = false
) {
    val ramUsagePercent: Int
        get() = if (ramTotalMb > 0) ((ramUsedMb * 100) / ramTotalMb).toInt() else 0
}

/**
 * Gaming Session record with performance stats and earned AURA🟣 points
 */
@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameTitle: String,
    val startTime: Long,
    val durationSeconds: Long = 0,
    val avgFps: Double = 60.0,
    val peakFps: Double = 60.0,
    val maxCpuTemp: Float = 42.0f,
    val maxGpuTemp: Float = 43.0f,
    val totalAuraEarned: Int = 0,
    val clutchMomentsCount: Int = 0
)

/**
 * AURA🟣 Reward Event record
 */
@Entity(tableName = "aura_events")
data class AuraEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pointsAwarded: Int = 100,
    val achievementTitle: String,
    val gameName: String,
    val description: String
)

/**
 * Predefined epic feats that trigger "+100 AURA🟣"
 */
data class ClutchFeat(
    val title: String,
    val titleBn: String,
    val icon: String,
    val points: Int = 100,
    val defaultDescription: String
)

val DEFAULT_CLUTCH_FEATS = listOf(
    ClutchFeat(
        title = "Clutch 1v4 Ace",
        titleBn = "১ বনাম ৪ ক্লাচ জয়",
        icon = "👑",
        points = 100,
        defaultDescription = "Alone against all odds, eliminated entire squad with ultimate precision!"
    ),
    ClutchFeat(
        title = "Triple Headshot Streak",
        titleBn = "ট্রিপল হেডশট স্ট্রাইক",
        icon = "🎯",
        points = 100,
        defaultDescription = "3 consecutive precision shots within 5 seconds!"
    ),
    ClutchFeat(
        title = "Sub-40°C Cool Matrix",
        titleBn = "আইস কুল থার্মাল বুস্ট",
        icon = "❄️",
        points = 100,
        defaultDescription = "Maintained device thermal stability under intense gaming load!"
    ),
    ClutchFeat(
        title = "Ultra Reflex Dodge",
        titleBn = "আল্ট্রা রিফ্লেক্স ডজ",
        icon = "⚡",
        points = 100,
        defaultDescription = "Surviving lethal damage with 1 HP remaining!"
    ),
    ClutchFeat(
        title = "Booyah / Victory Royale",
        titleBn = "চূড়ান্ত চ্যাম্পিয়ন বিজয়",
        icon = "🏆",
        points = 100,
        defaultDescription = "Secured #1 Rank with maximum combat effectiveness!"
    ),
    ClutchFeat(
        title = "60+ FPS Smooth Operator",
        titleBn = "সুপার স্মুথ এফপিএস পারফরম্যান্স",
        icon = "🚀",
        points = 100,
        defaultDescription = "Maintained rock-solid frame rate throughout the match!"
    )
)
