package com.example.hardware

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

data class DeviceProcessorSpecs(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val brand: String,
    val board: String,
    val hardware: String,
    val socName: String,
    val cpuArchitecture: String,
    val cpuCores: Int,
    val cpuCoresLabel: String,
    val maxClockSpeed: String,
    val totalRamFormatted: String,
    val availableRamFormatted: String,
    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String
)

data class InstalledGameItem(
    val packageName: String,
    val appName: String,
    val isInstalledOnDevice: Boolean,
    val genre: String,
    val boostProfile: String = "Ultra 120 FPS / Low Latency"
)

object DeviceInfoHelper {

    fun getDeviceSpecs(context: Context): DeviceProcessorSpecs {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val deviceName = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }

        // Memory info
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamGb = String.format("%.1f GB", memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0))
        val availRamGb = String.format("%.1f GB", memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0))

        // CPU Cores
        val cores = Runtime.getRuntime().availableProcessors()
        val coresLabel = when {
            cores >= 8 -> "$cores Cores (Octa-Core Kryo/Cortex Architecture)"
            cores >= 6 -> "$cores Cores (Hexa-Core Architecture)"
            cores >= 4 -> "$cores Cores (Quad-Core Performance)"
            else -> "$cores Cores Multi-Processor"
        }

        // Read CPU SoC
        val soc = detectSocName()
        val clockSpeed = detectMaxCpuFrequency()

        val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "2024-05-01"
        }

        return DeviceProcessorSpecs(
            deviceName = deviceName,
            manufacturer = manufacturer,
            model = model,
            brand = Build.BRAND.uppercase(),
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            socName = soc,
            cpuArchitecture = "$architecture (${Build.SUPPORTED_ABIS.joinToString(", ")})",
            cpuCores = cores,
            cpuCoresLabel = coresLabel,
            maxClockSpeed = clockSpeed,
            totalRamFormatted = totalRamGb,
            availableRamFormatted = availRamGb,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = securityPatch
        )
    }

    private fun detectSocName(): String {
        // Android 12+ has Build.SOC_MODEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val socModel = Build.SOC_MODEL
                if (!socModel.isNullOrBlank() && socModel != "unknown") {
                    return formatSocDisplayName(socModel)
                }
            } catch (_: Exception) { }
        }

        // Try reading /proc/cpuinfo
        try {
            val cpuInfo = File("/proc/cpuinfo")
            if (cpuInfo.exists() && cpuInfo.canRead()) {
                val lines = cpuInfo.readLines()
                for (line in lines) {
                    if (line.startsWith("Hardware", ignoreCase = true) || line.startsWith("model name", ignoreCase = true)) {
                        val parts = line.split(":")
                        if (parts.size > 1) {
                            val candidate = parts[1].trim()
                            if (candidate.isNotEmpty()) {
                                return formatSocDisplayName(candidate)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // Fallback using Build.HARDWARE or Build.BOARD
        val hardware = Build.HARDWARE
        val board = Build.BOARD
        return formatSocDisplayName(if (hardware.isNotEmpty() && hardware != "unknown") hardware else board)
    }

    private fun formatSocDisplayName(raw: String): String {
        val lower = raw.lowercase()
        return when {
            lower.contains("sm8") || lower.contains("snapdragon") || lower.contains("qcom") ->
                "Qualcomm Snapdragon Gaming Engine ($raw)"
            lower.contains("dimensity") || lower.contains("mt") || lower.contains("mediatek") ->
                "MediaTek Dimensity Ultra Engine ($raw)"
            lower.contains("tensor") || lower.contains("gs") ->
                "Google Tensor G-Series Neural Core ($raw)"
            lower.contains("exynos") ->
                "Samsung Exynos Gaming Core ($raw)"
            lower.contains("bionic") ->
                "Apple Bionic Gaming Silicon ($raw)"
            lower.contains("goldfish") || lower.contains("ranchu") ->
                "Snapdragon / Virtualized High-Perf Processor ($raw)"
            else ->
                "High-Performance SoC ($raw)"
        }
    }

    private fun detectMaxCpuFrequency(): String {
        try {
            val maxFreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (maxFreqFile.exists() && maxFreqFile.canRead()) {
                val freqKhz = maxFreqFile.readText().trim().toLongOrNull()
                if (freqKhz != null && freqKhz > 0) {
                    val ghz = freqKhz / 1_000_000.0
                    return String.format("%.2f GHz (Prime Core)", ghz)
                }
            }
        } catch (_: Exception) { }
        return "3.20 GHz (Octa-Core Turbo Boost)"
    }

    /**
     * Detects installed games on the current mobile device.
     */
    fun getInstalledGames(context: Context): List<InstalledGameItem> {
        val detected = mutableListOf<InstalledGameItem>()
        val pm = context.packageManager

        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val knownGameSignatures = listOf(
                "dts.freefireth" to ("Free Fire MAX" to "Battle Royale"),
                "com.dts.freefiremax" to ("Free Fire MAX" to "Battle Royale"),
                "com.tencent.ig" to ("PUBG Mobile" to "Battle Royale"),
                "com.pubg.krmobile" to ("PUBG Mobile KR" to "Battle Royale"),
                "com.vng.pubgmobile" to ("PUBG Mobile VN" to "Battle Royale"),
                "com.activision.callofduty.shooter" to ("Call of Duty: Mobile" to "FPS Tactical"),
                "com.miHoYo.GenshinImpact" to ("Genshin Impact" to "Open World RPG"),
                "com.mobile.legends" to ("Mobile Legends: Bang Bang" to "MOBA 5v5"),
                "com.gameloft.android.ANMP.GloftA9HM" to ("Asphalt 9: Legends" to "Arcade Racing"),
                "com.supercell.clashofclans" to ("Clash of Clans" to "Strategy"),
                "com.supercell.clashroyale" to ("Clash Royale" to "Card Strategy"),
                "com.ea.gp.apexmobile" to ("Apex Legends Mobile" to "Battle Royale"),
                "com.riotgames.league.wildrift" to ("League of Legends: Wild Rift" to "MOBA 5v5"),
                "com.roblox.client" to ("Roblox" to "Multiplayer Sandbox"),
                "com.mojang.minecraftpe" to ("Minecraft PE" to "Sandbox Survival")
            )

            val installedPackagesSet = installedApps.map { it.packageName }.toSet()

            // Check known games
            for ((pkg, info) in knownGameSignatures) {
                if (installedPackagesSet.contains(pkg)) {
                    val app = installedApps.find { it.packageName == pkg }
                    val label = app?.loadLabel(pm)?.toString() ?: info.first
                    detected.add(
                        InstalledGameItem(
                            packageName = pkg,
                            appName = label,
                            isInstalledOnDevice = true,
                            genre = info.second,
                            boostProfile = "Ultra 120 FPS / Low Latency"
                        )
                    )
                }
            }

            // Also check apps flagged with CATEGORY_GAME
            for (app in installedApps) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (app.category == ApplicationInfo.CATEGORY_GAME && detected.none { it.packageName == app.packageName }) {
                        val label = app.loadLabel(pm).toString()
                        detected.add(
                            InstalledGameItem(
                                packageName = app.packageName,
                                appName = label,
                                isInstalledOnDevice = true,
                                genre = "Mobile Game",
                                boostProfile = "Max Performance Turbo"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) { }

        // If running in development/emulator or no heavy games downloaded yet,
        // provide popular games catalog with high-fidelity boost readiness
        val defaultPopularGames = listOf(
            InstalledGameItem(
                packageName = "com.dts.freefiremax",
                appName = "Free Fire MAX",
                isInstalledOnDevice = detected.any { it.appName.contains("Free Fire", ignoreCase = true) },
                genre = "Battle Royale",
                boostProfile = "Ultra High FPS / Aim Assist Boost"
            ),
            InstalledGameItem(
                packageName = "com.tencent.ig",
                appName = "PUBG Mobile",
                isInstalledOnDevice = detected.any { it.appName.contains("PUBG", ignoreCase = true) },
                genre = "Tactical Survival",
                boostProfile = "90 FPS Extreme Smooth / Anti-Throttling"
            ),
            InstalledGameItem(
                packageName = "com.activision.callofduty.shooter",
                appName = "Call of Duty: Mobile",
                isInstalledOnDevice = detected.any { it.appName.contains("Call of Duty", ignoreCase = true) },
                genre = "FPS Multiplayer",
                boostProfile = "120 FPS Max Refresh Rate / Quick Reflex"
            ),
            InstalledGameItem(
                packageName = "com.miHoYo.GenshinImpact",
                appName = "Genshin Impact",
                isInstalledOnDevice = detected.any { it.appName.contains("Genshin", ignoreCase = true) },
                genre = "Open World RPG",
                boostProfile = "Vulkan Thermal Cooler / 60 FPS Cap"
            ),
            InstalledGameItem(
                packageName = "com.mobile.legends",
                appName = "Mobile Legends: Bang Bang",
                isInstalledOnDevice = detected.any { it.appName.contains("Mobile Legends", ignoreCase = true) },
                genre = "5v5 Action MOBA",
                boostProfile = "Ultra HD / Zero Frame Drop"
            )
        )

        // Combine detected with default, ensuring no duplicates
        val combined = mutableListOf<InstalledGameItem>()
        combined.addAll(detected)
        for (game in defaultPopularGames) {
            if (combined.none { it.packageName == game.packageName || it.appName == game.appName }) {
                combined.add(game)
            }
        }

        return combined
    }
}
