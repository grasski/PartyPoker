package com.dabi.partypoker.repository.gameSettings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameSettings(
    var isDefault: Boolean = true,
    var title: String = "Default",

    var playerMoney: Int = 1000,
    var smallBlindAmount: Int = 25,
    var bigBlindAmount: Int = 50,

    var playerTimerDurationMillis: Int = 8000,
    var gameOverTimerDurationMillis: Int = 7000,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)
