package com.dabi.partypoker.repository.gameSettings

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GameSettings::class],
    version = 1
)
abstract class GameSettingsDatabase: RoomDatabase() {
    abstract val dao: GameSettingsDao
}