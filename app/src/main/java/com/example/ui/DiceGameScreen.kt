package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantDieBg
import com.example.ui.theme.ElegantDieDot
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GameMatch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceGameScreen(
    viewModel: DiceGameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val historyMatches by viewModel.historyMatches.collectAsStateWithLifecycle()

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    // Auto-compute wins for P1 and P2 based on history matching their current names
    val p1Wins = historyMatches.count { it.winnerName.lowercase() == state.playerOneName.lowercase() }
    val p2Wins = historyMatches.count { it.winnerName.lowercase() == state.playerTwoName.lowercase() }

    // Rename dialog
    if (state.showRenameDialog != null) {
        RenamePlayerDialog(
            playerNumber = state.showRenameDialog!!,
            currentName = if (state.showRenameDialog == 1) state.playerOneName else state.playerTwoName,
            onConfirm = { num, name -> viewModel.updatePlayerName(num, name) },
            onDismiss = { viewModel.closeRenameDialog() }
        )
    }

    if (showRulesDialog) {
        GameRulesDialog(onDismiss = { showRulesDialog = false })
    }

    if (showHistoryDialog) {
        MatchHistoryDialog(
            matches = historyMatches,
            onClear = { viewModel.clearAllStats() },
            onDismiss = { showHistoryDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dice Duel",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(text = "v1.1", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier.testTag("rules_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Rules",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange, // fallback to standard available icon
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Select & General Target Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Mode Toggle Segment
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GameModeTabButton(
                        text = "Dice Race",
                        selected = state.gameMode == GameMode.RACE,
                        onClick = { viewModel.setGameMode(GameMode.RACE) },
                        tag = "mode_race_tab"
                    )
                    GameModeTabButton(
                        text = "Pig Mode",
                        selected = state.gameMode == GameMode.PIG,
                        onClick = { viewModel.setGameMode(GameMode.PIG) },
                        tag = "mode_pig_tab"
                    )
                }

                // Target score dropdown/selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Goal:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(30, 50, 100).forEach { score ->
                        val isSelected = state.targetScore == score
                        IconButton(
                            onClick = { viewModel.setTargetScore(score) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("target_${score}_button")
                        ) {
                            Text(
                                text = score.toString(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Lifetime Wins scorecard
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 Series Records",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${state.playerOneName}: $p1Wins wins  |  ${state.playerTwoName}: $p2Wins wins",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Dual Player Score Boards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player 1 Card
                PlayerScoreCard(
                    playerName = state.playerOneName,
                    playerScore = state.playerOneScore,
                    targetScore = state.targetScore,
                    isActive = state.activePlayer == 1 && state.winner == null,
                    isWinner = state.winner == 1,
                    turnScore = if (state.gameMode == GameMode.PIG && state.activePlayer == 1) state.turnScore else null,
                    onRenameClick = { viewModel.openRenameDialog(1) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTagPrefix = "p1"
                )

                // Player 2 Card
                PlayerScoreCard(
                    playerName = state.playerTwoName,
                    playerScore = state.playerTwoScore,
                    targetScore = state.targetScore,
                    isActive = state.activePlayer == 2 && state.winner == null,
                    isWinner = state.winner == 2,
                    turnScore = if (state.gameMode == GameMode.PIG && state.activePlayer == 2) state.turnScore else null,
                    onRenameClick = { viewModel.openRenameDialog(2) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    testTagPrefix = "p2"
                )
            }

            // Central Board representing current state/message or celebration
            AnimatedVisibility(
                visible = state.winner != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val winnerName = if (state.winner == 1) state.playerOneName else state.playerTwoName
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 WINNER! 🎉",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$winnerName sweeps the board with ${if (state.winner == 1) state.playerOneScore else state.playerTwoScore} pts!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }

            // Dice roll container and result indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Turn context / roll feedback messages
                    Text(
                        text = when {
                            state.winner != null -> "Match Over!"
                            state.isRolling -> "Shaking dice..."
                            state.wasBusted -> "Oops! Rolled 1 -> points busted!"
                            state.hasBonusTurn -> "Double! Free Bonus Roll!"
                            state.gameMode == GameMode.PIG -> "${if (state.activePlayer == 1) state.playerOneName else state.playerTwoName}'s Turn (Pig Mode)"
                            else -> "${if (state.activePlayer == 1) state.playerOneName else state.playerTwoName}'s Turn (Roll!)"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = when {
                            state.wasBusted -> MaterialTheme.colorScheme.error
                            state.hasBonusTurn -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // In Pig Mode we only roll 1 die, let's keep things elegant and symmetric or single-focused
                        if (state.gameMode == GameMode.PIG) {
                            DieView(
                                value = state.lastRolledValue1,
                                isRolling = state.isRolling,
                                modifier = Modifier.testTag("primary_die")
                            )
                        } else {
                            DieView(
                                value = state.lastRolledValue1,
                                isRolling = state.isRolling,
                                modifier = Modifier.testTag("die_1")
                            )
                            DieView(
                                value = state.lastRolledValue2,
                                isRolling = state.isRolling,
                                modifier = Modifier.testTag("die_2")
                            )
                        }
                    }
                }
            }

            // Action Button bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Roll Button
                Button(
                    onClick = { viewModel.rollDice() },
                    enabled = !state.isRolling && state.winner == null,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("roll_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.hasBonusTurn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Roll")
                        Text(
                            text = if (state.hasBonusTurn) "BONUS ROLL!" else "ROLL DICE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Hold button (Only in Pig Mode)
                AnimatedVisibility(
                    visible = state.gameMode == GameMode.PIG,
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { viewModel.holdPoints() },
                        enabled = !state.isRolling && state.winner == null && state.turnScore > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("hold_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ThumbUp, contentDescription = "Hold") // standard fallback
                            Text(
                                text = "HOLD POINTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Reset Button
                OutlinedButton(
                    onClick = { viewModel.resetMatch() },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Match",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick scrollable mini roll logger of recent turns
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📝 Live Logs",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.rollHistory.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No rolls recorded in this match. Spin to start!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.rollHistory) { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = if (selected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier
            .height(38.dp)
            .testTag(tag)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlayerScoreCard(
    playerName: String,
    playerScore: Int,
    targetScore: Int,
    isActive: Boolean,
    isWinner: Boolean,
    turnScore: Int?,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "p"
) {
    val progress = (playerScore.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)

    // Glowing active state transition border animations
    val pulseAlpha by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val cardBorder = if (isActive) {
        Spacer(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha))
                .fillMaxSize()
        )
    } else null

    Card(
        modifier = modifier
            .testTag("${testTagPrefix}_score_card")
            .graphicsLayer {
                if (isActive) {
                    scaleX = 1.02f
                    scaleY = 1.02f
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWinner -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                isActive -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 6.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Name and Edit Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRenameClick,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("${testTagPrefix}_rename_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Player",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Central Massive Score Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = playerScore.toString(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isWinner) Color(0xFF81C784) else (if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
                    lineHeight = 56.sp
                )
                Text(
                    text = "Goal: $targetScore",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Real-time track indicator or turn accumulator in Pig mode
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (turnScore != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Holding: +$turnScore pts",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "YOUR TURN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Score track Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("${testTagPrefix}_progress"),
                    color = if (isWinner) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun DieView(value: Int, isRolling: Boolean, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            launch {
                rotation.animateTo(
                    targetValue = rotation.value + 720f,
                    animationSpec = tween(durationMillis = 480, easing = LinearEasing)
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 0.75f,
                    animationSpec = tween(durationMillis = 240, easing = FastOutLinearInEasing)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    Card(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                rotationZ = rotation.value
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDieBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = size.height * 0.09f
                val dotColor = ElegantDieDot

                val center = size.width / 2f
                val left = size.width * 0.25f
                val right = size.width * 0.75f
                val top = size.height * 0.25f
                val bottom = size.height * 0.75f

                // Precise visual mapping of classical dice dots
                when (value) {
                    1 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(center, center))
                    }
                    2 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                    }
                    3 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(center, center))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                    }
                    4 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                    }
                    5 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(center, center))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                    }
                    6 -> {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, top))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, center))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, center))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(left, bottom))
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(right, bottom))
                    }
                }
            }
        }
    }
}

@Composable
fun RenamePlayerDialog(
    playerNumber: Int,
    currentName: String,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("rename_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rename Player $playerNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 15) newName = it },
                    singleLine = true,
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("rename_cancel_btn")) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onConfirm(playerNumber, newName)
                            }
                        },
                        modifier = Modifier.testTag("rename_confirm_btn")
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun GameRulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎲 Dice Duel Rules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = "Mode 1: Dice Race (Fast & Luck-Based)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "• Players take turns rolling TWO dice.\n" +
                            "• Your score increases by the sum of both dice.\n" +
                            "• DOUBLE BONUS: Roll matching doubles to fetch the sum + a complimentary +5 pts reward AND a free extra roll immediately!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mode 2: Pig Mode (Highly Strategic)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "• Active player rolls ONE die repeatedly.\n" +
                            "• Roll 2-6: Adds value to your turn accumulator.\n" +
                            "• Roll 1 -> BUSTED! Lose all accumulated turn points and immediately pass your turn to opponent.\n" +
                            "• Click 'HOLD POINTS' to bank the accumulator into your permanent score and switch turns safely.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}

@Composable
fun MatchHistoryDialog(
    matches: List<GameMatch>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(450.dp)
                .testTag("history_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📜 Match History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (matches.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.testTag("clear_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                if (matches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No games played yet.\nComplete a match to record outcomes!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(matches) { match ->
                            HistoryItemRow(match = match)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(match: GameMatch) {
    val simpleDateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val dateString = simpleDateFormat.format(Date(match.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "${match.playerOneName} vs ${match.playerTwoName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Score: ${match.playerOneScore} - ${match.playerTwoScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${match.gameMode} • $dateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Winner:\n${match.winnerName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
