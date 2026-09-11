package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hardware.DeviceInfoHelper
import com.example.hardware.DeviceProcessorSpecs
import com.example.hardware.InstalledGameItem
import com.example.ui.GoogleAccountState
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

@Composable
fun SettingsTab(
    googleAccount: GoogleAccountState,
    deviceSpecs: DeviceProcessorSpecs,
    installedGames: List<InstalledGameItem>,
    isOverlayServiceRunning: Boolean,
    onToggleGoogleAccount: () -> Unit,
    onUpdateGoogleAccount: (String, String) -> Unit,
    onToggleOverlayService: (Context) -> Unit,
    onLaunchGame: (Context, InstalledGameItem) -> Unit,
    onRefreshGames: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditAccountDialog by remember { mutableStateOf(false) }

    val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else true

    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 1. Creator & App Version Header Card
        item {
            CreatorIdentityCard()
        }

        // 2. Google Account Section
        item {
            GoogleAccountSection(
                account = googleAccount,
                onEditClick = { showEditAccountDialog = true },
                onToggleConnect = onToggleGoogleAccount
            )
        }

        // 3. Floating In-Game HUD Overlay Section
        item {
            InGameOverlaySection(
                hasPermission = hasOverlayPermission,
                isServiceRunning = isOverlayServiceRunning,
                onToggleOverlay = { onToggleOverlayService(context) },
                onOpenPermissionSettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        context.startActivity(intent)
                    }
                }
            )
        }

        // 4. Installed Games on Mobile Device Section
        item {
            InstalledGamesSection(
                games = installedGames,
                onLaunchGame = { game -> onLaunchGame(context, game) },
                onRefresh = { onRefreshGames(context) }
            )
        }

        // 5. Mobile & Detailed Processor Specifications Card
        item {
            DeviceProcessorSpecsCard(specs = deviceSpecs)
        }

        // 6. Required App Permissions List
        item {
            AppPermissionsCard(
                hasOverlayPermission = hasOverlayPermission,
                hasNotificationPermission = hasNotificationPermission,
                onOpenOverlaySettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        context.startActivity(intent)
                    }
                },
                onOpenAppSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }

    if (showEditAccountDialog) {
        EditGoogleAccountDialog(
            currentEmail = googleAccount.email,
            currentName = googleAccount.displayName,
            onDismiss = { showEditAccountDialog = false },
            onConfirm = { email, name ->
                onUpdateGoogleAccount(email, name)
                showEditAccountDialog = false
            }
        )
    }
}

/**
 * Prominent Creator Badge and App Version v1.0
 */
@Composable
fun CreatorIdentityCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(AuraCyanNeon, AuraPurpleLight, AuraGold)),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardDark)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Developer Emblem
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AuraPurple, AuraCyanNeon))
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👑",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User requested: "ক্রিয়েটেড বাই RIDWAN SARWAR"
                Text(
                    text = "CREATED BY RIDWAN SARWAR",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = AuraCyanNeon,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Aura Gaming Booster Architect & Optimization Engine",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // User requested: "অ্যাপে ভার্শন দিও অনেক ছোট্ট করে ওয়ান পয়েন্ট জিরো"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1B4B))
                        .border(1.dp, AuraPurpleGlow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "v1.0 (Build 100)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraPurpleGlow,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Google Account Integration Section
 */
@Composable
fun GoogleAccountSection(
    account: GoogleAccountState,
    onEditClick: () -> Unit,
    onToggleConnect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Google Account / গুগল একাউন্ট",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (account.isConnected) "Cloud Sync Connected 🟣" else "Not Connected",
                            fontSize = 11.sp,
                            color = if (account.isConnected) AuraGreenNeon else Color.Gray
                        )
                    }
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Account",
                        tint = AuraCyanNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (account.isConnected) {
                // Connected Account Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AuraPurple, AuraCyanNeon))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = account.email,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "AURA🟣 Points Cloud Backup: Active",
                            fontSize = 10.sp,
                            color = AuraGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onToggleConnect,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Disconnect", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AuraPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Switch Account", fontSize = 12.sp, color = Color.White)
                    }
                }
            } else {
                // Connect Button
                Button(
                    onClick = onToggleConnect,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sign in with Google", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Floating In-Game HUD Screen Overlay Section
 */
@Composable
fun InGameOverlaySection(
    hasPermission: Boolean,
    isServiceRunning: Boolean,
    onToggleOverlay: () -> Unit,
    onOpenPermissionSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isServiceRunning) AuraCyanNeon else CyberBorder,
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraCyanNeon.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AuraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "In-Game Floating Screen HUD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "যে কোন গেমের স্ক্রিনের ওপর লাইভ চালু করুন",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = { onToggleOverlay() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AuraCyanNeon,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "এটি চালু করলে Free Fire, PUBG বা যেকোনো গেম খেলার সময় স্ক্রিনের ওপর লাইভ FPS, ফ্রেম টাইম এবং CPU/GPU টেম্পারেচার ভেসে থাকবে।",
                fontSize = 12.sp,
                color = Color.LightGray,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!hasPermission) {
                // Missing Permission Warning & Quick Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraRedCritical.copy(alpha = 0.15f))
                        .border(1.dp, AuraRedCritical.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AuraRedCritical,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Draw Over Apps Permission Required",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "অন্য অ্যাপ ও গেমের ওপর দেখানোর পারমিশন প্রয়োজন।",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onOpenPermissionSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AuraCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Enable Overlay Permission", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraGreenNeon.copy(alpha = 0.12f))
                        .border(1.dp, AuraGreenNeon.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AuraGreenNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceRunning) "Floating HUD is active on screen right now! ⚡" else "Permission Granted ✅ Ready to float over games",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onToggleOverlay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) Color(0xFFEF4444) else AuraPurple
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isServiceRunning) "🛑 Stop Floating HUD" else "🚀 Start Floating HUD Now",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Mobile Installed Games Section
 */
@Composable
fun InstalledGamesSection(
    games: List<InstalledGameItem>,
    onLaunchGame: (InstalledGameItem) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraPurple.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = AuraPurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Installed Games on Phone",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "মোবাইলে ইনস্টল থাকা গেম সমূহ (${games.size} টি পাওয়া গেছে)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Games",
                        tint = AuraCyanNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                games.forEach { game ->
                    InstalledGameRow(
                        game = game,
                        onLaunch = { onLaunchGame(game) }
                    )
                }
            }
        }
    }
}

@Composable
fun InstalledGameRow(
    game: InstalledGameItem,
    onLaunch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    )
                )
                .border(1.dp, AuraPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎮",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = game.appName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                if (game.isInstalledOnDevice) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AuraGreenNeon.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "INSTALLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraGreenNeon
                        )
                    }
                }
            }
            Text(
                text = "${game.genre} • ${game.boostProfile}",
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }

        Button(
            onClick = onLaunch,
            colors = ButtonDefaults.buttonColors(containerColor = AuraPurple),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (game.isInstalledOnDevice) "🚀 Launch" else "📲 Get & Play",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Mobile Device Model & Detailed Processor Specifications Card
 */
@Composable
fun DeviceProcessorSpecsCard(specs: DeviceProcessorSpecs) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AuraGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = AuraGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Device & Processor Specifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "যেকোনো মোবাইলের হার্ডওয়্যার ও প্রসেসরের বিস্তারিত তথ্য",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Highlighted Device Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, AuraPurpleLight.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = AuraCyanNeon,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = specs.deviceName,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Brand: ${specs.brand} | Board: ${specs.board}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Hardware Specs Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecItemRow(
                    icon = Icons.Default.Memory,
                    label = "Processor / SoC Engine",
                    value = specs.socName,
                    valueColor = AuraCyanNeon
                )
                SpecItemRow(
                    icon = Icons.Default.Speed,
                    label = "CPU Architecture & ABI",
                    value = specs.cpuArchitecture,
                    valueColor = Color.White
                )
                SpecItemRow(
                    icon = Icons.Default.DeveloperBoard,
                    label = "CPU Cores",
                    value = specs.cpuCoresLabel,
                    valueColor = AuraPurpleLight
                )
                SpecItemRow(
                    icon = Icons.Default.Speed,
                    label = "Max CPU Clock Frequency",
                    value = specs.maxClockSpeed,
                    valueColor = AuraGreenNeon
                )
                SpecItemRow(
                    icon = Icons.Default.Memory,
                    label = "Total System Memory (RAM)",
                    value = "${specs.totalRamFormatted} (Available: ${specs.availableRamFormatted})",
                    valueColor = AuraGold
                )
                SpecItemRow(
                    icon = Icons.Default.Security,
                    label = "Android OS & API Level",
                    value = "${specs.androidVersion} (API ${specs.apiLevel})",
                    valueColor = Color.White
                )
                SpecItemRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Security Patch Level",
                    value = specs.securityPatch,
                    valueColor = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun SpecItemRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

/**
 * Required App Permissions & Security Card
 */
@Composable
fun AppPermissionsCard(
    hasOverlayPermission: Boolean,
    hasNotificationPermission: Boolean,
    onOpenOverlaySettings: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AuraPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AuraPurpleLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Required Permissions / পারমিশন সমূহ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "অ্যাপের সঠিক কার্যকারিতার জন্য প্রয়োজনীয় অনুমতি",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionStatusRow(
                    name = "Display Over Other Apps (SYSTEM_ALERT_WINDOW)",
                    description = "গেম খেলা অবস্থায় লাইভ FPS ও তাপমাত্রার ভাসমান HUD দেখানোর জন্য।",
                    isGranted = hasOverlayPermission,
                    onActionClick = onOpenOverlaySettings
                )
                PermissionStatusRow(
                    name = "Notifications (POST_NOTIFICATIONS)",
                    description = "গেমিং বুস্টার ব্যাকগ্রাউন্ড সার্ভিস ও পারফরম্যান্স স্ট্যাটাসের জন্য।",
                    isGranted = hasNotificationPermission,
                    onActionClick = onOpenAppSettings
                )
                PermissionStatusRow(
                    name = "Haptic Vibration (VIBRATE)",
                    description = "+১০০ ওরা পয়েন্ট এবং ১-ট্যাপ বুস্টিংয়ে ভাইব্রেশন রেসপন্সের জন্য।",
                    isGranted = true,
                    onActionClick = null
                )
                PermissionStatusRow(
                    name = "Foreground Game Monitor Service",
                    description = "সেশন চলাকালীন সিপিইউ ও জিপিইউ থার্মাল অপ্টিমাইজেশন ট্র্যাকিং।",
                    isGranted = true,
                    onActionClick = null
                )
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    name: String,
    description: String,
    isGranted: Boolean,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, if (isGranted) Color(0xFF1E293B) else AuraRedCritical.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) AuraGreenNeon else AuraRedCritical,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }

        if (!isGranted && onActionClick != null) {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = AuraCyan),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        } else {
            Text(
                text = "Granted",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraGreenNeon
            )
        }
    }
}

/**
 * Edit / Switch Google Account Dialog
 */
@Composable
fun EditGoogleAccountDialog(
    currentEmail: String,
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var emailInput by remember { mutableStateOf(currentEmail) }
    var nameInput by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberCardDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google Account সেট করুন",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "আপনার গুগল একাউন্ট ইমেইল ও নাম প্রদান করুন যা AURA🟣 ক্লাউড সিঙ্কের সাথে যুক্ত থাকবে:",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AuraCyanNeon,
                        unfocusedBorderColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Google Email") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AuraCyanNeon,
                        unfocusedBorderColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(emailInput, nameInput) },
                colors = ButtonDefaults.buttonColors(containerColor = AuraPurple)
            ) {
                Text("Save & Sync", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
