package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashFinished: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    
    // Rotation of the neon dice
    val rotation = remember { Animatable(0f) }
    // Scaling of the neon dice
    val scale = remember { Animatable(0f) }
    // Fade-in of text
    val textAlpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Run animations concurrently
        // 1. Zoom in and spin the die with spring bounce
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(1200, easing = EaseOutCubic)
        )
        
        // 2. Fade in the title text
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = LinearEasing)
        )
        
        // 3. Simulate loading bar progress
        while (progress < 1f) {
            delay(20)
            progress += 0.015f
        }
        
        // Subtle delay at completion for organic feel
        delay(300)
        
        // Exit
        onSplashFinished()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E142B), // Deep space indigo
                        Color(0xFF0C0812)  // Near-black purple
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 2000f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Materialistic Dice pair
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // Let's draw a single premium dark greyish / metallic dice using Compose Canvas!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Size of die face
                    val dieSizePx = 68.dp.toPx()
                    val halfSize = dieSizePx / 2f
                    val offsetVal = halfSize * 0.52f

                    drawContext.canvas.save()
                    // Translate to outer center
                    drawContext.canvas.translate(w / 2f, h / 2f)
                    // Apply animated spin
                    drawContext.canvas.rotate(rotation.value)

                    // Draw single heavy drop shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(-halfSize + 4.dp.toPx(), -halfSize + 4.dp.toPx()),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    // Draw dark steel gradient die face
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF48484A), // Medium slate gray
                                Color(0xFF1C1C1E)  // Dark carbon gray
                            ),
                            start = Offset(-halfSize, -halfSize),
                            end = Offset(halfSize, halfSize)
                        ),
                        topLeft = Offset(-halfSize, -halfSize),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )

                    // Draw subtle glowing metallic rim/border
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF8E8E93), // Silver accent
                                Color(0xFF2C2C2E)  // Dim background
                            )
                        ),
                        topLeft = Offset(-halfSize, -halfSize),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    // Draw neon/glowing dots for 5
                    val dotRadius = 4.5.dp.toPx()
                    val outerNeonGlow = Color(0xFFD0BCFF).copy(alpha = 0.4f)
                    val coreLimeColor = Color(0xFFE8DEF8) // Glowing lavender light

                    // Helper function to draw a single glowing dot
                    fun drawGlowingDot(center: Offset) {
                        // Ambient glow aura
                        drawCircle(
                            color = outerNeonGlow,
                            radius = dotRadius * 2f,
                            center = center
                        )
                        // Core solid dot
                        drawCircle(
                            color = coreLimeColor,
                            radius = dotRadius,
                            center = center
                        )
                    }

                    // Top-Left Dot
                    drawGlowingDot(Offset(-offsetVal, -offsetVal))
                    // Top-Right Dot
                    drawGlowingDot(Offset(offsetVal, -offsetVal))
                    // Center Dot
                    drawGlowingDot(Offset(0f, 0f))
                    // Bottom-Left Dot
                    drawGlowingDot(Offset(-offsetVal, offsetVal))
                    // Bottom-Right Dot
                    drawGlowingDot(Offset(offsetVal, offsetVal))

                    drawContext.canvas.restore()
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title with staggered/faded visual
            Text(
                text = "DICE",
                fontSize = 38.sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = textAlpha.value }
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "ROLL OF DESTINY",
                fontSize = 11.sp,
                color = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = textAlpha.value }
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Sleek glowing progress loader
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(180.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = Color(0xFFD0BCFF),
                    trackColor = Color(0xFF2B2930),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${(progress * 100).toInt().coerceIn(0, 100)}%",
                    color = Color(0xFFCAC4D0).copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
