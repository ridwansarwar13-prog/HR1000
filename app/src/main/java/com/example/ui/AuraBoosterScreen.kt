package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_CLUTCH_FEATS
import com.example.ui.components.AuraCelebration3DOverlay
import com.example.ui.components.Card3DContainer
import com.example.ui.components.CornerFpsHud
import com.example.ui.components.GojoAuraCard
import com.example.ui.components.HologramGauge3D
import com.example.ui.components.RotatingAuraCore3D
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanNeon
import com.example.ui.theme.AuraGold
import com.example.ui.theme.AuraGreenNeon
import com.example.ui.theme.AuraOrangeThermal
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraPurpleGlow
import com.example.ui.theme.AuraPurpleLight
import com.example.ui.theme.AuraRedCritical
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import com.example.ui.theme.CyberSurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraBoosterScreen(
    viewModel: AuraBoosterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val totalAura by viewModel.totalAuraPoints.collectAsState()
    val gameSession by viewModel.gameSession.collectAsState()
    val boostState by viewModel.boostState.collectAsState()
    val celebrationVisible by viewModel.celebrationVisible.collectAsState()
    val latestAchievement by viewModel.latestAchievement.collectAsState()
    val isCornerHudActive by viewModel.isCornerHudActive.collectAsState()
    val recentEvents by viewModel.recentAuraEvents.collectAsState()
    val googleAccount by viewModel.googleAccount.collectAsState()
    val deviceSpecs by viewModel.deviceSpecs.collectAsState()
    val installedGames by viewModel.installedGames.collectAsState()
    val isOverlayServiceRunning by viewModel.isOverlayServiceRunning.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGameName by remember { mutableStateOf("Free Fire MAX") }
    var customFeatText by remember { mutableStateOf("") }

    val popularGames = listOf("Free Fire MAX", "PUBG Mobile", "Call of Duty: M", "Genshin Impact", "Mobile Legends")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBgDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aura Gaming Booster",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AuraPurple.copy(alpha = 0.35f))
                                .border(1.dp, AuraPurpleLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AURA🟣",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraPurpleGlow
                            )
                        }
                    }
                },
                actions = {
                    // Quick Toggle for Floating Corner HUD
                    IconButton(onClick = { viewModel.toggleCornerHud() }) {
                        Icon(
                            imageVector = if (isCornerHudActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Corner FPS HUD",
                            tint = if (isCornerHudActive) AuraCyanNeon else Color.Gray
                        )
                    }
                    // Direct Settings shortcut
                    IconButton(onClick = { selectedTab = 3 }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedTab == 3) AuraCyanNeon else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurfaceDark,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CyberSurfaceDark,
                    contentColor = AuraPurpleLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = AuraPurpleGlow,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "🔥 THERMAL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTab == 0) AuraCyanNeon else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🎮 GAMES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTab == 1) AuraPurpleGlow else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "🟣 REWARDS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTab == 2) AuraGold else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                "⚙️ SETTINGS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (selectedTab == 3) AuraCyanNeon else Color.Gray
                            )
                        }
                    )
                }

                // Tab Contents
                when (selectedTab) {
                    0 -> ThermalFpsTab(
                        telemetry = telemetry,
                        totalAura = totalAura,
                        auraRank = viewModel.getAuraRank(totalAura),
                        boostState = boostState,
                        onBoostClick = { viewModel.performCyberBoost() },
                        isCornerHudActive = isCornerHudActive,
                        onToggleCornerHud = { viewModel.toggleCornerHud() },
                        onToggleSystemOverlay = { viewModel.toggleSystemOverlay(context) }
                    )
                    1 -> GameMonitorTab(
                        gameSession = gameSession,
                        popularGames = popularGames,
                        selectedGame = selectedGameName,
                        onSelectGame = { selectedGameName = it },
                        onToggleSession = { viewModel.toggleGameSession(selectedGameName) },
                        onAwardFeat = { feat ->
                            viewModel.awardAuraPoints(feat.title, feat.titleBn, feat.points)
                        },
                        customFeatText = customFeatText,
                        onCustomFeatTextChange = { customFeatText = it },
                        onAddCustomFeat = {
                            if (customFeatText.isNotBlank()) {
                                viewModel.awardAuraPoints(
                                    featTitle = customFeatText,
                                    featTitleBn = customFeatText,
                                    points = 100
                                )
                                customFeatText = ""
                            }
                        }
                    )
                    2 -> AuraRewardsTab(
                        totalAura = totalAura,
                        auraRank = viewModel.getAuraRank(totalAura),
                        recentEvents = recentEvents,
                        onQuickAward = {
                            viewModel.awardAuraPoints(
                                featTitle = "Manual Clutch Play",
                                featTitleBn = "ম্যানুয়াল দুর্দান্ত ক্লাচ",
                                points = 100
                            )
                        }
                    )
                    3 -> com.example.ui.components.SettingsTab(
                        googleAccount = googleAccount,
                        deviceSpecs = deviceSpecs,
                        installedGames = installedGames,
                        isOverlayServiceRunning = isOverlayServiceRunning,
                        onToggleGoogleAccount = { viewModel.toggleGoogleAccount() },
                        onUpdateGoogleAccount = { email, name -> viewModel.updateGoogleAccount(email, name) },
                        onToggleOverlayService = { ctx -> viewModel.toggleSystemOverlay(ctx) },
                        onLaunchGame = { ctx, game -> viewModel.launchGameWithBooster(ctx, game) },
                        onRefreshGames = { ctx -> viewModel.refreshInstalledGames(ctx) }
                    )
                }
            }

            // In-App Corner Screen FPS & Temperature HUD (Requested by user)
            if (isCornerHudActive) {
                CornerFpsHud(
                    telemetry = telemetry,
                    totalAuraPoints = totalAura,
                    onClose = { viewModel.toggleCornerHud() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                )
            }

            // 3D Celebration Overlay for "+100 AURA🟣"
            AuraCelebration3DOverlay(
                visible = celebrationVisible,
                featTitle = latestAchievement,
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}

/**
 * Tab 1: Real-Time Hardware Thermal (CPU/GPU) & FPS Telemetry
 */
@Composable
fun ThermalFpsTab(
    telemetry: com.example.data.model.HardwareTelemetry,
    totalAura: Int,
    auraRank: String,
    boostState: BoostState,
    onBoostClick: () -> Unit,
    isCornerHudActive: Boolean,
    onToggleCornerHud: () -> Unit,
    onToggleSystemOverlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gojo Hollow Purple Mascot Card ("গুজর ছবি" as requested)
        item {
            GojoAuraCard(
                totalAuraPoints = totalAura,
                auraRank = auraRank
            )
        }

        // 3D Holographic Dial Gauges (Dual: CPU Temp & Live FPS)
        item {
            Card3DContainer(
                borderGlowColor = AuraCyanNeon,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3D THERMAL & FPS TELEMETRY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AuraCyanNeon,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (telemetry.thermalStatusName) {
                                        "OPTIMAL" -> AuraGreenNeon.copy(alpha = 0.2f)
                                        "MODERATE" -> AuraOrangeThermal.copy(alpha = 0.2f)
                                        else -> AuraRedCritical.copy(alpha = 0.25f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STATUS: ${telemetry.thermalStatusName}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (telemetry.thermalStatusName) {
                                    "OPTIMAL" -> AuraGreenNeon
                                    "MODERATE" -> AuraOrangeThermal
                                    else -> AuraRedCritical
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // CPU Temperature 3D Gauge
                        HologramGauge3D(
                            value = telemetry.cpuTemperature,
                            maxValue = 75f,
                            unit = "°C",
                            title = "CPU TEMP",
                            accentColor = if (telemetry.cpuTemperature >= 48f) AuraRedCritical else AuraOrangeThermal,
                            sizeDp = 145.dp
                        )

                        // Real-time FPS 3D Gauge
                        HologramGauge3D(
                            value = telemetry.fps.toFloat(),
                            maxValue = 120f,
                            unit = "FPS",
                            title = "LIVE FRAMES",
                            accentColor = AuraCyanNeon,
                            sizeDp = 145.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary telemetry specs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0B1E))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GPU TEMP", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%.1f°C".format(telemetry.gpuTemperature), fontSize = 14.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BATTERY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%.1f°C".format(telemetry.batteryTemperature), fontSize = 14.sp, color = AuraGreenNeon, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FRAME TIME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%.1f ms".format(telemetry.frameTimeMs), fontSize = 14.sp, color = AuraCyanNeon, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RAM USAGE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("${telemetry.ramUsagePercent}%", fontSize = 14.sp, color = AuraPurpleLight, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // 1-Tap 3D Cyber Boost Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "1-TAP CYBER BOOST",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Optimize memory & cool down thermal load",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        Button(
                            onClick = onBoostClick,
                            enabled = !boostState.isBoosting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraPurple),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = "Boost", tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (boostState.isBoosting) "BOOSTING..." else "BOOST NOW",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (boostState.lastBoostMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚡ ${boostState.lastBoostMessage}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraCyanNeon
                        )
                    }
                }
            }
        }

        // Screen Corner HUD Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "স্ক্রিন কর্নার এফপিএস ও টেম্পারেচার HUD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "স্ক্রিনের কোণায় রিয়েল-টাইমে FPS এবং CPU/GPU তাপমাত্রা দেখার অপশন",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onToggleCornerHud,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCornerHudActive) AuraCyanNeon.copy(alpha = 0.25f) else Color(0xFF221A3B)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCornerHudActive) AuraCyanNeon else Color.Gray
                            )
                        ) {
                            Text(
                                text = if (isCornerHudActive) "ইন-অ্যাপ HUD (চালু)" else "ইন-অ্যাপ HUD চালু করুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCornerHudActive) AuraCyanNeon else Color.White
                            )
                        }

                        Button(
                            onClick = onToggleSystemOverlay,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraPurple)
                        ) {
                            Text(
                                text = "গেমের উপর ফ্লোটিং HUD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Game Monitor & Clutch Feats (+100 AURA🟣)
 */
@Composable
fun GameMonitorTab(
    gameSession: GameSessionUiState,
    popularGames: List<String>,
    selectedGame: String,
    onSelectGame: (String) -> Unit,
    onToggleSession: () -> Unit,
    onAwardFeat: (com.example.data.model.ClutchFeat) -> Unit,
    customFeatText: String,
    onCustomFeatTextChange: (String) -> Unit,
    onAddCustomFeat: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Game Session Monitor Card
        item {
            Card3DContainer(
                borderGlowColor = if (gameSession.isSessionActive) AuraGreenNeon else AuraPurple,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (gameSession.isSessionActive) AuraGreenNeon else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (gameSession.isSessionActive) "গেম মনিটর সেশন চলছে" else "গেম মনিটর বন্ধ আছে",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onToggleSession,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (gameSession.isSessionActive) AuraRedCritical else AuraGreenNeon
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (gameSession.isSessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (gameSession.isSessionActive) "সেশন শেষ করুন" else "মনিটর শুরু করুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Game Selector chips
                    Text(
                        text = "গেম নির্বাচন করুন:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(popularGames) { game ->
                            val isSelected = game == selectedGame
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AuraPurple else Color(0xFF1E1838))
                                    .clickable { onSelectGame(game) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = game,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Session Metrics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F0B1E))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val minutes = gameSession.sessionDurationSeconds / 60
                        val seconds = gameSession.sessionDurationSeconds % 60
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সময়", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%02d:%02d".format(minutes, seconds), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("গড় FPS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%.1f".format(gameSession.avgFps), fontSize = 14.sp, color = AuraCyanNeon, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("সর্বোচ্চ CPU", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("%.1f°C".format(gameSession.maxCpuTemp), fontSize = 14.sp, color = AuraOrangeThermal, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("অর্জিত ওরা", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("+${gameSession.sessionAuraEarned} 🟣", fontSize = 14.sp, color = AuraPurpleGlow, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Clutch Plays / Epic Feats Header
        item {
            Column {
                Text(
                    text = "দুর্দান্ত গেম অ্যাকশন রেকর্ড করুন (+100 AURA🟣)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "গেম চলাকালীন অসাধারণ কাজ করলে ট্যাপ করুন, সাথে সাথে +১০০ ওরা পাবেন!",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        // Grid of Clutch Feats
        items(DEFAULT_CLUTCH_FEATS) { feat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAwardFeat(feat) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = feat.icon, fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${feat.titleBn} (${feat.title})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = feat.defaultDescription,
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AuraPurple, Color(0xFF7C3AED))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+100 AURA🟣",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Custom Clutch Play input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "কাস্টম দুর্দান্ত কাজ যোগ করুন:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customFeatText,
                            onValueChange = onCustomFeatTextChange,
                            placeholder = { Text("যেমন: স্নাইপার দিয়ে ৩ জনকে উড়িয়ে দিলাম", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAddCustomFeat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AuraPurple)
                        ) {
                            Text("+১০০ ওরা", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Aura Reward Logs & Rank Details
 */
@Composable
fun AuraRewardsTab(
    totalAura: Int,
    auraRank: String,
    recentEvents: List<com.example.data.model.AuraEventEntity>,
    onQuickAward: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GojoAuraCard(
                totalAuraPoints = totalAura,
                auraRank = auraRank
            )
        }

        item {
            Card3DContainer(
                borderGlowColor = AuraGold,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RotatingAuraCore3D(sizeDp = 90.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "AURA🟣 পয়েন্ট সিস্টেম",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "আপনার বর্তমান ব্যালেন্স: %,d AURA🟣".format(totalAura),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraPurpleGlow
                    )
                    Text(
                        text = "গেম মনিটরে প্রতিটি দুর্দান্ত কাজ ও স্টেবল পারফরম্যান্সে আপনি পাবেন ১০০ ওরা পয়েন্ট!",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = onQuickAward,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AuraPurple)
                    ) {
                        Text(
                            text = "ক্লেইম করুন +100 AURA🟣",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "সাম্প্রতিক অর্জিত ওরা হিস্ট্রি (${recentEvents.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (recentEvents.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "এখনো কোন ইভেন্ট লগ হয়নি। গেম মনিটরে দুর্দান্ত কাজ করে +১০০ ওরা সংগ্রহ করুন!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(recentEvents) { event ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.achievementTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${event.gameName} • ${event.description}",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuraPurple.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${event.pointsAwarded} AURA🟣",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraPurpleGlow
                            )
                        }
                    }
                }
            }
        }
    }
}
