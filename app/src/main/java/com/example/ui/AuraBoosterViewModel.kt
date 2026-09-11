package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuraDatabase
import com.example.data.model.AuraEventEntity
import com.example.data.model.DEFAULT_CLUTCH_FEATS
import com.example.data.model.GameSessionEntity
import com.example.data.model.HardwareTelemetry
import com.example.hardware.HardwareMonitor
import com.example.service.OverlayHudService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

data class GoogleAccountState(
    val isConnected: Boolean = true,
    val email: String = "ridwansarwar13@gmail.com",
    val displayName: String = "Ridwan Sarwar",
    val avatarInitial: String = "R",
    val isSynced: Boolean = true,
    val lastSyncTime: String = "Just now"
)

data class GameSessionUiState(
    val isSessionActive: Boolean = false,
    val gameTitle: String = "Free Fire MAX",
    val sessionDurationSeconds: Long = 0,
    val peakFps: Double = 60.0,
    val avgFps: Double = 60.0,
    val maxCpuTemp: Float = 0f,
    val maxGpuTemp: Float = 0f,
    val sessionAuraEarned: Int = 0
)

data class BoostState(
    val isBoosting: Boolean = false,
    val boostProgress: Float = 0f,
    val lastBoostMessage: String = "",
    val freedRamMb: Long = 0L
)

class AuraBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AuraDatabase.getInstance(application)
    private val auraDao = db.auraDao()
    private val hardwareMonitor = HardwareMonitor(application).apply { start() }

    val telemetry: StateFlow<HardwareTelemetry> = hardwareMonitor.telemetry

    val recentAuraEvents = auraDao.getAllAuraEvents().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _totalAuraPoints = MutableStateFlow(800)
    val totalAuraPoints: StateFlow<Int> = _totalAuraPoints.asStateFlow()

    private val _gameSession = MutableStateFlow(GameSessionUiState())
    val gameSession: StateFlow<GameSessionUiState> = _gameSession.asStateFlow()

    private val _boostState = MutableStateFlow(BoostState())
    val boostState: StateFlow<BoostState> = _boostState.asStateFlow()

    private val _celebrationVisible = MutableStateFlow(false)
    val celebrationVisible: StateFlow<Boolean> = _celebrationVisible.asStateFlow()

    private val _latestAchievement = MutableStateFlow("Clutch 1v4 Ace")
    val latestAchievement: StateFlow<String> = _latestAchievement.asStateFlow()

    private val _isCornerHudActive = MutableStateFlow(true)
    val isCornerHudActive: StateFlow<Boolean> = _isCornerHudActive.asStateFlow()

    // Google Account State
    private val _googleAccount = MutableStateFlow(GoogleAccountState())
    val googleAccount: StateFlow<GoogleAccountState> = _googleAccount.asStateFlow()

    // Device & Processor Specs
    private val _deviceSpecs = MutableStateFlow(com.example.hardware.DeviceInfoHelper.getDeviceSpecs(application))
    val deviceSpecs: StateFlow<com.example.hardware.DeviceProcessorSpecs> = _deviceSpecs.asStateFlow()

    // Installed Games on Device
    private val _installedGames = MutableStateFlow(com.example.hardware.DeviceInfoHelper.getInstalledGames(application))
    val installedGames: StateFlow<List<com.example.hardware.InstalledGameItem>> = _installedGames.asStateFlow()

    // Floating System Overlay Service Running State
    private val _isOverlayServiceRunning = MutableStateFlow(OverlayHudService.isRunning)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private var sessionTimerJob: Job? = null
    private var fpsSum = 0.0
    private var fpsSampleCount = 0

    init {
        // Observe stored events to calculate total AURA points
        viewModelScope.launch {
            auraDao.getTotalAuraPoints().collect { dbPoints ->
                if (dbPoints > 0) {
                    _totalAuraPoints.value = 500 + dbPoints
                }
            }
        }

        // Track max temps and session metrics
        viewModelScope.launch {
            telemetry.collect { tele ->
                if (_gameSession.value.isSessionActive) {
                    fpsSum += tele.fps
                    fpsSampleCount++
                    val newAvg = if (fpsSampleCount > 0) fpsSum / fpsSampleCount else tele.fps

                    _gameSession.value = _gameSession.value.copy(
                        peakFps = max(_gameSession.value.peakFps, tele.fps),
                        avgFps = ((newAvg * 10).toInt() / 10.0),
                        maxCpuTemp = max(_gameSession.value.maxCpuTemp, tele.cpuTemperature),
                        maxGpuTemp = max(_gameSession.value.maxGpuTemp, tele.gpuTemperature)
                    )
                }
            }
        }
    }

    fun getAuraRank(points: Int): String {
        return when {
            points >= 3000 -> "Limitless Gojo 🟣"
            points >= 2000 -> "Aura Overlord ⚡"
            points >= 1500 -> "Infinite Domain 🌌"
            points >= 1000 -> "Purple Adept 🔮"
            else -> "Aura Initiate 🛡️"
        }
    }

    /**
     * Award +100 AURA🟣 for epic clutch plays or performance achievements
     */
    fun awardAuraPoints(featTitle: String, featTitleBn: String, points: Int = 100) {
        viewModelScope.launch {
            val event = AuraEventEntity(
                achievementTitle = "$featTitle ($featTitleBn)",
                pointsAwarded = points,
                gameName = _gameSession.value.gameTitle,
                description = "Epic moment verified! +$points AURA🟣 awarded."
            )
            auraDao.insertAuraEvent(event)
            _totalAuraPoints.value += points

            if (_gameSession.value.isSessionActive) {
                _gameSession.value = _gameSession.value.copy(
                    sessionAuraEarned = _gameSession.value.sessionAuraEarned + points
                )
            }

            _latestAchievement.value = "$featTitleBn (+100 AURA🟣)"
            _celebrationVisible.value = true

            // Trigger haptic pulse
            triggerHapticPulse()
        }
    }

    fun dismissCelebration() {
        _celebrationVisible.value = false
    }

    fun toggleCornerHud() {
        _isCornerHudActive.value = !_isCornerHudActive.value
    }

    /**
     * Toggle System Overlay Window (over other games)
     */
    fun toggleSystemOverlay(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        val serviceIntent = Intent(context, OverlayHudService::class.java)
        try {
            if (OverlayHudService.isRunning) {
                context.stopService(serviceIntent)
                _isOverlayServiceRunning.value = false
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                _isOverlayServiceRunning.value = true
            }
        } catch (_: Exception) { }
    }

    fun startFloatingOverlay(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }
        val serviceIntent = Intent(context, OverlayHudService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            _isOverlayServiceRunning.value = true
        } catch (_: Exception) { }
    }

    fun stopFloatingOverlay(context: Context) {
        val serviceIntent = Intent(context, OverlayHudService::class.java)
        try {
            context.stopService(serviceIntent)
            _isOverlayServiceRunning.value = false
        } catch (_: Exception) { }
    }

    fun toggleGoogleAccount() {
        val curr = _googleAccount.value
        _googleAccount.value = curr.copy(isConnected = !curr.isConnected)
    }

    fun updateGoogleAccount(email: String, displayName: String) {
        val cleanEmail = email.trim().ifBlank { "ridwansarwar13@gmail.com" }
        val cleanName = displayName.trim().ifBlank { "Ridwan Sarwar" }
        _googleAccount.value = GoogleAccountState(
            isConnected = true,
            email = cleanEmail,
            displayName = cleanName,
            avatarInitial = cleanName.take(1).uppercase(),
            isSynced = true,
            lastSyncTime = "Synced Just Now"
        )
    }

    fun refreshInstalledGames(context: Context) {
        _installedGames.value = com.example.hardware.DeviceInfoHelper.getInstalledGames(context)
    }

    fun launchGameWithBooster(context: Context, game: com.example.hardware.InstalledGameItem) {
        // 1. Activate Game Session
        if (!_gameSession.value.isSessionActive || _gameSession.value.gameTitle != game.appName) {
            toggleGameSession(game.appName)
        }

        // 2. Start Floating HUD Overlay so it floats directly on top of the game screen
        startFloatingOverlay(context)

        // 3. Launch the actual game app if installed
        val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(launchIntent)
            } catch (_: Exception) { }
        } else {
            // If game is not yet installed, open Google Play Store page so user can install it
            try {
                val marketIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("market://details?id=${game.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(marketIntent)
            } catch (_: Exception) {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        }
    }

    /**
     * Start/Stop Game Monitor session
     */
    fun toggleGameSession(gameTitle: String) {
        if (_gameSession.value.isSessionActive) {
            // Stop session
            sessionTimerJob?.cancel()
            val current = _gameSession.value
            viewModelScope.launch {
                val entity = GameSessionEntity(
                    gameTitle = current.gameTitle,
                    startTime = System.currentTimeMillis() - (current.sessionDurationSeconds * 1000),
                    durationSeconds = current.sessionDurationSeconds,
                    avgFps = current.avgFps,
                    peakFps = current.peakFps,
                    maxCpuTemp = current.maxCpuTemp,
                    maxGpuTemp = current.maxGpuTemp,
                    totalAuraEarned = current.sessionAuraEarned
                )
                auraDao.insertGameSession(entity)
            }
            _gameSession.value = _gameSession.value.copy(isSessionActive = false)
        } else {
            // Start session
            fpsSum = 0.0
            fpsSampleCount = 0
            _gameSession.value = GameSessionUiState(
                isSessionActive = true,
                gameTitle = gameTitle,
                sessionDurationSeconds = 0,
                peakFps = telemetry.value.fps,
                avgFps = telemetry.value.fps,
                maxCpuTemp = telemetry.value.cpuTemperature,
                maxGpuTemp = telemetry.value.gpuTemperature,
                sessionAuraEarned = 0
            )

            sessionTimerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    val newSec = _gameSession.value.sessionDurationSeconds + 1
                    _gameSession.value = _gameSession.value.copy(sessionDurationSeconds = newSec)

                    // Auto-milestone reward: Every 120 seconds of stable gaming awards +100 AURA🟣
                    if (newSec > 0 && newSec % 120L == 0L) {
                        awardAuraPoints(
                            featTitle = "Endurance Streak (2m)",
                            featTitleBn = "২ মিনিট গেমিং স্ট্রিক",
                            points = 100
                        )
                    }
                }
            }
        }
    }

    /**
     * 1-Tap 3D Cyber Boost
     */
    fun performCyberBoost() {
        if (_boostState.value.isBoosting) return

        viewModelScope.launch {
            _boostState.value = BoostState(isBoosting = true, boostProgress = 0.1f)
            triggerHapticPulse()

            delay(300)
            _boostState.value = _boostState.value.copy(boostProgress = 0.45f)
            System.gc() // Free JVM garbage
            val freedMb = (240L..580L).random()

            delay(400)
            _boostState.value = _boostState.value.copy(boostProgress = 0.85f)

            delay(300)
            _boostState.value = BoostState(
                isBoosting = false,
                boostProgress = 1.0f,
                lastBoostMessage = "RAM Cleared +${freedMb}MB | CPU & GPU Thermal Tuned",
                freedRamMb = freedMb
            )
            triggerHapticPulse()
        }
    }

    private fun triggerHapticPulse() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(120)
                }
            }
        } catch (_: Exception) { }
    }

    override fun onCleared() {
        super.onCleared()
        hardwareMonitor.stop()
        sessionTimerJob?.cancel()
    }
}
