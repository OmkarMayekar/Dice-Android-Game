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
                // Let's draw two layered physical dice using Compose Canvas!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Size of die face
                    val dieSizePx = 54.dp.toPx()
                    val halfSize = dieSizePx / 2f
                    val offsetVal = halfSize * 0.5f

                    // 1. Back Die: White/Silver Die (Rotated, e.g., -18 degrees)
                    drawContext.canvas.save()
                    drawContext.canvas.translate(w * 0.38f, h * 0.38f)
                    drawContext.canvas.rotate(-18f)

                    // Draw White Die Base Shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(-halfSize + 2.dp.toPx(), -halfSize + 2.dp.toPx()),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )

                    // Draw White Die Base
                    drawRoundRect(
                        color = Color(0xFFF5F5F7), // Soft clean white
                        topLeft = Offset(-halfSize, -halfSize),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )
                    // Draw White Die Border
                    drawRoundRect(
                        color = Color(0xFFD2D2D7),
                        topLeft = Offset(-halfSize, -halfSize),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw dots for 6
                    val r6 = 3.5.dp.toPx()
                    val dotColor6 = Color(0xFF1D1D1F) // Deep obsidian black dots
                    // Left column dots
                    drawCircle(color = dotColor6, radius = r6, center = Offset(-offsetVal, -offsetVal))
                    drawCircle(color = dotColor6, radius = r6, center = Offset(-offsetVal, 0f))
                    drawCircle(color = dotColor6, radius = r6, center = Offset(-offsetVal, offsetVal))
                    // Right column dots
                    drawCircle(color = dotColor6, radius = r6, center = Offset(offsetVal, -offsetVal))
                    drawCircle(color = dotColor6, radius = r6, center = Offset(offsetVal, 0f))
                    drawCircle(color = dotColor6, radius = r6, center = Offset(offsetVal, offsetVal))

                    drawContext.canvas.restore()

                    // 2. Front Die: Vivid Red Die (Rotated 15 degrees + animated rot)
                    drawContext.canvas.save()
                    drawContext.canvas.translate(w * 0.62f, h * 0.62f)
                    drawContext.canvas.rotate(15f + (rotation.value * 0.5f)) // animated spin

                    // Drop shadow for the front die on top of the back die
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(-halfSize + 3.dp.toPx(), -halfSize + 3.dp.toPx()),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )

                    // Draw Red Die Base (Gradient from warm red to crimson)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF453A), Color(0xFFD32F2F)) // Modern Material Red
                        ),
                        topLeft = Offset(-halfSize, -halfSize),
                        size = Size(dieSizePx, dieSizePx),
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                    )

                    // Draw dots for 5
                    val r5 = 4.dp.toPx()
                    val dotColor5 = Color.White // Brilliant white dots
                    // Corner dots
                    drawCircle(color = dotColor5, radius = r5, center = Offset(-offsetVal, -offsetVal))
                    drawCircle(color = dotColor5, radius = r5, center = Offset(offsetVal, -offsetVal))
                    drawCircle(color = dotColor5, radius = r5, center = Offset(-offsetVal, offsetVal))
                    drawCircle(color = dotColor5, radius = r5, center = Offset(offsetVal, offsetVal))
                    // Center dot
                    drawCircle(color = dotColor5, radius = r5, center = Offset(0f, 0f))

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
