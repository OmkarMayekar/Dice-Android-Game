package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameMatch
import com.example.data.GameMatchRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameMode {
    RACE, PIG
}

data class GameState(
    val playerOneName: String = "Player 1",
    val playerTwoName: String = "Player 2",
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val turnScore: Int = 0, // Pig mode temporary points accumulator
    val activePlayer: Int = 1, // 1 or 2
    val lastRolledValue1: Int = 4,
    val lastRolledValue2: Int = 3,
    val isRolling: Boolean = false,
    val gameMode: GameMode = GameMode.RACE,
    val targetScore: Int = 50,
    val winner: Int? = null, // 1 or 2 when someone wins
    val rollHistory: List<String> = emptyList(), // Recent rolls log for current match
    val showRenameDialog: Int? = null, // 1 or 2 to prompt name entry
    val hasBonusTurn: Boolean = false, // Race Double Roll bonus indication
    val wasBusted: Boolean = false // Track when Pig mode player rolls a 1
)

class DiceGameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameMatchRepository
    val historyMatches: StateFlow<List<GameMatch>>

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameMatchRepository(database.gameMatchDao())
        historyMatches = repository.allMatches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun setGameMode(mode: GameMode) {
        val defaultTarget = if (mode == GameMode.PIG) 100 else 50
        _gameState.update {
            it.copy(
                gameMode = mode,
                targetScore = defaultTarget
            )
        }
        resetMatch()
    }

    fun setTargetScore(score: Int) {
        _gameState.update { it.copy(targetScore = score) }
        resetMatch()
    }

    fun updatePlayerName(playerNumber: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        _gameState.update {
            if (playerNumber == 1) {
                it.copy(playerOneName = trimmed, showRenameDialog = null)
            } else {
                it.copy(playerTwoName = trimmed, showRenameDialog = null)
            }
        }
    }

    fun openRenameDialog(playerNumber: Int) {
        _gameState.update { it.copy(showRenameDialog = playerNumber) }
    }

    fun closeRenameDialog() {
        _gameState.update { it.copy(showRenameDialog = null) }
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.isRolling || state.winner != null) return

        _gameState.update { it.copy(isRolling = true, wasBusted = false, hasBonusTurn = false) }

        viewModelScope.launch {
            // Dice rolling simulation animation delays
            var tempVal1 = 1
            var tempVal2 = 1
            repeat(8) {
                tempVal1 = Random.nextInt(1, 7)
                tempVal2 = Random.nextInt(1, 7)
                _gameState.update {
                    it.copy(
                        lastRolledValue1 = tempVal1,
                        lastRolledValue2 = tempVal2
                    )
                }
                delay(60)
            }

            // Real outcome roll
            val dice1 = Random.nextInt(1, 7)
            val dice2 = Random.nextInt(1, 7)

            _gameState.update { current ->
                val active = current.activePlayer
                val activeName = if (active == 1) current.playerOneName else current.playerTwoName

                if (current.gameMode == GameMode.RACE) {
                    // DICE RACE RULES:
                    // sum of 2 dice is added to player's score.
                    // If rolling doubles (dice1 == dice2), they get matching score + 5 bonus points and another turn!
                    val sum = dice1 + dice2
                    val isDouble = dice1 == dice2
                    val rollSumTotal = if (isDouble) sum + 5 else sum

                    val logText = if (isDouble) {
                        "$activeName rolled Double $dice1! Total: $rollSumTotal pts (+5 Bonus) & gets to roll again!"
                    } else {
                        "$activeName rolled $dice1 & $dice2. Total: $sum"
                    }

                    val updatedP1Score = if (active == 1) current.playerOneScore + rollSumTotal else current.playerOneScore
                    val updatedP2Score = if (active == 2) current.playerTwoScore + rollSumTotal else current.playerTwoScore

                    // Check win
                    var winnerResult: Int? = null
                    if (updatedP1Score >= current.targetScore) winnerResult = 1
                    else if (updatedP2Score >= current.targetScore) winnerResult = 2

                    val nextPlayer = if (winnerResult != null) {
                        active // Game won, keep active
                    } else if (isDouble) {
                        active // Double roll reward -> active gets to roll again
                    } else {
                        if (active == 1) 2 else 1 // Switch turn
                    }

                    val updatedHistory = listOf(logText) + current.rollHistory.take(15)

                    current.copy(
                        lastRolledValue1 = dice1,
                        lastRolledValue2 = dice2,
                        playerOneScore = updatedP1Score,
                        playerTwoScore = updatedP2Score,
                        activePlayer = nextPlayer,
                        isRolling = false,
                        winner = winnerResult,
                        rollHistory = updatedHistory,
                        hasBonusTurn = isDouble && winnerResult == null
                    )
                } else {
                    // PIG DICE RULES:
                    // Rolls 1 die (we use dice1 and ignore dice2 in terms of score, but can animate it too, or keep dice2 hidden/shown appropriately).
                    // If rolled 1, they lose their turn accumulators and turn ends immediately.
                    // Else, rolled value is added to turn accumulation.
                    val logText: String
                    var nextPlayer = active
                    var updatedTurnScore = current.turnScore
                    var wasBusted = false

                    if (dice1 == 1) {
                        logText = "$activeName rolled a 1! BUSTED. Lost accumulated points."
                        updatedTurnScore = 0
                        nextPlayer = if (active == 1) 2 else 1
                        wasBusted = true
                    } else {
                        updatedTurnScore += dice1
                        logText = "$activeName rolled a $dice1. Turn accumulator: $updatedTurnScore"
                    }

                    val updatedHistory = listOf(logText) + current.rollHistory.take(15)

                    current.copy(
                        lastRolledValue1 = dice1,
                        lastRolledValue2 = dice2, // stored for visual symmetry
                        turnScore = updatedTurnScore,
                        activePlayer = nextPlayer,
                        isRolling = false,
                        rollHistory = updatedHistory,
                        wasBusted = wasBusted
                    )
                }
            }

            // If a winner emerged in Race Mode, save match
            _gameState.value.winner?.let { winnerNum ->
                saveMatchToDatabase()
            }
        }
    }

    fun holdPoints() {
        val current = _gameState.value
        if (current.gameMode != GameMode.PIG || current.winner != null || current.isRolling) return

        val active = current.activePlayer
        val activeName = if (active == 1) current.playerOneName else current.playerTwoName
        val accumulated = current.turnScore

        if (accumulated == 0) return // Nothing to bank

        _gameState.update { state ->
            val updatedP1Score = if (active == 1) state.playerOneScore + accumulated else state.playerOneScore
            val updatedP2Score = if (active == 2) state.playerTwoScore + accumulated else state.playerTwoScore

            // Check win
            var winnerResult: Int? = null
            if (updatedP1Score >= state.targetScore) winnerResult = 1
            else if (updatedP2Score >= state.targetScore) winnerResult = 2

            val nextPlayer = if (winnerResult != null) active else (if (active == 1) 2 else 1)
            val logText = "$activeName banked $accumulated points! (Total: ${if (active == 1) updatedP1Score else updatedP2Score} pts)"
            val updatedHistory = listOf(logText) + state.rollHistory.take(15)

            state.copy(
                playerOneScore = updatedP1Score,
                playerTwoScore = updatedP2Score,
                turnScore = 0,
                activePlayer = nextPlayer,
                winner = winnerResult,
                rollHistory = updatedHistory
            )
        }

        // If a winner emerged, save match
        _gameState.value.winner?.let {
            saveMatchToDatabase()
        }
    }

    private fun saveMatchToDatabase() {
        val state = _gameState.value
        val winnerName = if (state.winner == 1) state.playerOneName else state.playerTwoName
        val match = GameMatch(
            playerOneName = state.playerOneName,
            playerTwoName = state.playerTwoName,
            playerOneScore = state.playerOneScore,
            playerTwoScore = state.playerTwoScore,
            winnerName = winnerName,
            gameMode = state.gameMode.name,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertMatch(match)
        }
    }

    fun resetMatch() {
        _gameState.update {
            it.copy(
                playerOneScore = 0,
                playerTwoScore = 0,
                turnScore = 0,
                activePlayer = 1,
                winner = null,
                rollHistory = emptyList(),
                wasBusted = false,
                hasBonusTurn = false
            )
        }
    }

    fun clearAllStats() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
