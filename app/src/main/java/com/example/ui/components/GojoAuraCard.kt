package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AuraCyanNeon
import com.example.ui.theme.AuraGold
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraPurpleGlow
import com.example.ui.theme.AuraPurpleLight
import com.example.ui.theme.CyberCardDark

@Composable
fun GojoAuraCard(
    totalAuraPoints: Int,
    auraRank: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GojoAuraHalo")
    val auraGlowRadius by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Card3DContainer(
        modifier = modifier.fillMaxWidth(),
        borderGlowColor = AuraPurpleLight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Gojo Avatar with Animated Hollow Purple Halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(92.dp)
            ) {
                // Pulsing Aura Energy Halo
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer {
                            scaleX = auraGlowRadius
                            scaleY = auraGlowRadius
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(AuraPurpleGlow.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // Gojo Hero Image
                Image(
                    painter = painterResource(id = R.drawable.img_gojo_aura),
                    contentDescription = "Gojo Satoru Hollow Purple Aura Mascot",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(
                            2.5.dp,
                            Brush.sweepGradient(
                                listOf(AuraPurpleLight, AuraCyanNeon, AuraPurpleGlow)
                            ),
                            CircleShape
                        )
                )

                // 3D floating mini badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1035))
                        .border(1.dp, AuraPurpleLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🟣 MAX",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = AuraPurpleLight
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Aura Stats & Domain Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AURA🟣 POWER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraCyanNeon,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AuraPurple.copy(alpha = 0.3f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = auraRank,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "%,d AURA🟣".format(totalAuraPoints),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Aura progress bar to next tier
                val currentTierMax = ((totalAuraPoints / 500) + 1) * 500
                val progress = ((totalAuraPoints % 500).toFloat() / 500f).coerceIn(0.05f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF271F42))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AuraCyanNeon, AuraPurpleGlow)
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "পরবর্তী লেভেলে পৌঁছাতে আর ${currentTierMax - totalAuraPoints} ওরা প্রয়োজন",
                    fontSize = 10.sp,
                    color = Color.LightGray.copy(alpha = 0.8f)
                )
            }
        }
    }
}
