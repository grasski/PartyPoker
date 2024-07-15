package com.dabi.partypoker.repository.gameSettings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface GameSettingsDao {
    @Upsert
    suspend fun upsertSetting(settings: GameSettings): Long

    @Delete
    suspend fun deleteSetting(settings: GameSettings)

    @Query("SELECT * FROM GameSettings")
    fun getAllSettings(): Flow<List<GameSettings>>

    @Query("SELECT * FROM GameSettings WHERE id = :id")
    fun getSettingById(id: Long): Flow<GameSettings?>

}