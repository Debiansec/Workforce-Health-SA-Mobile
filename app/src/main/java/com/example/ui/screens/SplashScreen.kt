package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0C0E),
                        Color(0xFF13161C),
                        Color(0xFF200A0C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Logo Container
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF0EFEA))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wfh_logo),
                    contentDescription = "WorkForce Health SA Official Logo",
                    modifier = Modifier.width(220.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "WORKFORCE HEALTH SA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Occupational Hygiene Digital Platform",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = WfhRedVibrant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = WfhSurfaceElevated.copy(alpha = 0.8f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
            ) {
                Text(
                    text = "Working within the Law!",
                    color = WfhGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Three Core Pillars (Control, Evaluation, Prevention)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillarBadge("CONTROL")
                Text("•", color = WfhTextMuted)
                PillarBadge("EVALUATION")
                Text("•", color = WfhTextMuted)
                PillarBadge("PREVENTION")
            }

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = WfhRedPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("splash_progress_indicator")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Initialising Offline Storage & Module Engine...",
                color = WfhTextMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PillarBadge(text: String) {
    Surface(
        color = Color(0x33C4121A),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66C4121A))
    ) {
        Text(
            text = text,
            color = Color(0xFFFFDAD6),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
