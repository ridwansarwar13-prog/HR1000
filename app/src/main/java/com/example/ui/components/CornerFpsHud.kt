package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HardwareTelemetry
import com.example.ui.theme.AuraCyanNeon
import com.example.ui.theme.AuraGreenNeon
import com.example.ui.theme.AuraOrangeThermal
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraPurpleGlow
import com.example.ui.theme.AuraPurpleLight
import com.example.ui.theme.AuraRedCritical
import kotlin.math.roundToInt

/**
 * Real-time Corner Screen Overlay HUD
 * Displays FPS, CPU °C, GPU °C, and AURA🟣 right on the corner of the screen
 */
@Composable
fun CornerFpsHud(
    telemetry: HardwareTelemetry,
    totalAuraPoints: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xE60D0B18))
                .border(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(AuraCyanNeon, AuraPurpleGlow, AuraPurple)
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Drag handle / FPS badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF22163B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (telemetry.fps >= 55) AuraGreenNeon else AuraOrangeThermal)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.0f".format(telemetry.fps),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = AuraCyanNeon
                            )
                        }
                        Text(
                            text = "FPS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // CPU Temp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f°C".format(telemetry.cpuTemperature),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.cpuTemperature >= 48f) AuraRedCritical else AuraOrangeThermal
                            )
                            Text(
                                text = "CPU TEMP",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }

                        // GPU Temp
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1f°C".format(telemetry.gpuTemperature),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (telemetry.gpuTemperature >= 48f) AuraRedCritical else Color(0xFFFFD54F)
                            )
                            Text(
                                text = "GPU TEMP",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }

                        // AURA Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AuraPurple.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "%,d AURA🟣".format(totalAuraPoints),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraPurpleLight
                            )
                        }

                        // Close corner button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A1C44))
                                .clickable { onClose() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
