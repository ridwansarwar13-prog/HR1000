package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AuraEventEntity
import com.example.data.model.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuraEvent(event: AuraEventEntity): Long

    @Query("SELECT * FROM aura_events ORDER BY timestamp DESC")
    fun getAllAuraEvents(): Flow<List<AuraEventEntity>>

    @Query("SELECT COALESCE(SUM(pointsAwarded), 0) FROM aura_events")
    fun getTotalAuraPoints(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameSession(session: GameSessionEntity): Long

    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllGameSessions(): Flow<List<GameSessionEntity>>
}
