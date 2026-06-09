package com.greencode27.dice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_matches")
data class GameMatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerOneName: String,
    val playerTwoName: String,
    val playerOneScore: Int,
    val playerTwoScore: Int,
    val winnerName: String,
    val gameMode: String, // "RACE" or "PIG"
    val timestamp: Long = System.currentTimeMillis()
)
