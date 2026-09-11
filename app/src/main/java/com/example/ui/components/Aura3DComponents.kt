package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraCyanNeon
import com.example.ui.theme.AuraGold
import com.example.ui.theme.AuraGreenNeon
import com.example.ui.theme.AuraOrangeThermal
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraPurpleGlow
import com.example.ui.theme.AuraPurpleLight
import com.example.ui.theme.AuraRedCritical
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardDark
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Interactive 3D Card Container that tilts in 3D space with touch gestures and ambient floating motion
 */
@Composable
fun Card3DContainer(
    modifier: Modifier = Modifier,
    borderGlowColor: Color = AuraPurple,
    content: @Composable () -> Unit
) {
    var rotX by remember { mutableFloatStateOf(0f) }
    var rotY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "3DCardFloat")
    val ambientY by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientY"
    )

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        rotX = 0f
                        rotY = 0f
                    },
                    onDragCancel = {
                        rotX = 0f
                        rotY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        rotY = (rotY + dragAmount.x * 0.08f).coerceIn(-18f, 18f)
                        rotX = (rotX - dragAmount.y * 0.08f).coerceIn(-18f, 18f)
                    }
                )
            }
            .graphicsLayer {
                rotationX = rotX + ambientY
                rotationY = rotY
                cameraDistance = 14f * density
                shadowElevation = 16.dp.toPx()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardDark),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    borderGlowColor.copy(alpha = 0.8f),
                    borderGlowColor.copy(alpha = 0.2f),
                    AuraCyanNeon.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        content()
    }
}

/**
 * 3D Hologram Dial Gauge with rotating cyber-rings and neon gradient arc
 */
@Composable
fun HologramGauge3D(
    value: Float,
    maxValue: Float = 100f,
    unit: String = "°C",
    title: String,
    accentColor: Color = AuraOrangeThermal,
    sizeDp: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HoloDialRings")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ),
        label = "ringRot"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val sweepAngle = ((value / maxValue).coerceIn(0f, 1f)) * 240f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                cameraDistance = 12f * density
                rotationX = 10f
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val strokeWidth = 10.dp.toPx()
            val canvasSize = size.minDimension
            val radius = (canvasSize - strokeWidth) / 2
            val centerOffset = Offset(size.width / 2, size.height / 2)

            // Outer decorative segmented ring
            drawCircle(
                color = accentColor.copy(alpha = 0.15f),
                radius = radius + 6.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Background arc (240 deg, starts at 150 deg)
            drawArc(
                color = Color(0xFF231D3D),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground glowing gradient arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        AuraCyanNeon,
                        accentColor,
                        if (value > 48f) AuraRedCritical else AuraPurpleLight
                    )
                ),
                startAngle = 150f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glowing indicator tick marks
            val tickCount = 18
            for (i in 0..tickCount) {
                val tickAngle = 150f + (240f / tickCount) * i
                val rad = Math.toRadians(tickAngle.toDouble())
                val innerR = radius - 14.dp.toPx()
                val outerR = radius - 8.dp.toPx()
                val start = Offset(
                    (centerOffset.x + innerR * cos(rad)).toFloat(),
                    (centerOffset.y + innerR * sin(rad)).toFloat()
                )
                val end = Offset(
                    (centerOffset.x + outerR * cos(rad)).toFloat(),
                    (centerOffset.y + outerR * sin(rad)).toFloat()
                )
                drawLine(
                    color = if (i.toFloat() / tickCount <= (value / maxValue)) accentColor.copy(alpha = pulseAlpha) else Color.DarkGray.copy(alpha = 0.4f),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Inner 3D spinning cyber ticks
        Box(
            modifier = Modifier
                .size(sizeDp * 0.58f)
                .graphicsLayer {
                    rotationZ = ringRotation
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    radius = r
                )
                for (angle in 0 until 360 step 60) {
                    val rad = Math.toRadians(angle.toDouble())
                    val p1 = Offset((size.width / 2 + (r - 4.dp.toPx()) * cos(rad)).toFloat(), (size.height / 2 + (r - 4.dp.toPx()) * sin(rad)).toFloat())
                    val p2 = Offset((size.width / 2 + r * cos(rad)).toFloat(), (size.height / 2 + r * sin(rad)).toFloat())
                    drawLine(AuraCyan.copy(alpha = 0.5f), p1, p2, 2.dp.toPx())
                }
            }
        }

        // Center Digital Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "%.1f".format(value),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = unit,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 3D Rotating AURA🟣 Core Sphere with concentric energy waves
 */
@Composable
fun RotatingAuraCore3D(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 100.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraCore3D")
    val rotZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(6000, easing = LinearEasing)),
        label = "rotZ"
    )
    val rotY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing)),
        label = "rotY"
    )
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scalePulse
                scaleY = scalePulse
                rotationY = rotY
                rotationZ = rotZ
                cameraDistance = 10f * density
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2
            // Core purple aura glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuraPurpleGlow,
                        AuraPurple.copy(alpha = 0.6f),
                        Color(0xFF3B0764).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                radius = r
            )
            // Cyber Orbit Rings
            drawCircle(
                color = AuraCyanNeon.copy(alpha = 0.7f),
                radius = r * 0.82f,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = AuraPurpleLight.copy(alpha = 0.9f),
                radius = r * 0.62f,
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        Text(
            text = "🟣",
            fontSize = 32.sp,
            modifier = Modifier.graphicsLayer {
                rotationY = -rotY
                rotationZ = -rotZ
            }
        )
    }
}

/**
 * 3D Celebration Overlay for "+100 AURA🟣" with Bengali announcement
 * "আপনার একশো ওরা প্লাস হয়েছে"
 */
@Composable
fun AuraCelebration3DOverlay(
    visible: Boolean,
    featTitle: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CelebrationSpin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing)),
        label = "spin"
    )

    val scaleAnim = remember { Animatable(0.4f) }

    LaunchedEffect(visible) {
        if (visible) {
            scaleAnim.snapTo(0.4f)
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f)
            )
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .pointerInput(Unit) {
                    detectDragGestures { _, _ -> onDismiss() }
                }
        ) {
            // 3D Background energy particles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                for (i in 0..24) {
                    val angle = (i * 15f + spinAngle)
                    val rad = Math.toRadians(angle.toDouble())
                    val dist = (120 + (i * 12) % 180).dp.toPx()
                    val p = Offset(
                        (center.x + dist * cos(rad)).toFloat(),
                        (center.y + dist * sin(rad)).toFloat()
                    )
                    drawCircle(
                        color = if (i % 2 == 0) AuraPurpleGlow else AuraCyanNeon,
                        radius = (3 + i % 4).dp.toPx(),
                        center = p
                    )
                }
            }

            // Central 3D Badge
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(0.92f)
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        rotationX = 6f
                        cameraDistance = 14f * density
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160E2E)),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Brush.sweepGradient(listOf(AuraPurpleGlow, AuraCyanNeon, AuraGold, AuraPurpleGlow))
                )
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RotatingAuraCore3D(sizeDp = 90.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bengali Celebration Announcement as requested
                    Text(
                        text = "আপনার ১০০ ওরা প্লাস হয়েছে!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(AuraPurple, Color(0xFF7C3AED))))
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+100 AURA🟣",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = featTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraCyanNeon,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "ম্যাচে অবিশ্বাস্য পারফরম্যান্স রেকর্ড করা হয়েছে! আপনার ওরা র‍্যাঙ্ক বৃদ্ধি পেয়েছে।",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = AuraPurple
                        ),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text(
                            text = "CLAIM AURA🟣",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
