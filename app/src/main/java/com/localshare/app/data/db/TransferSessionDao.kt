package com.localshare.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferSessionDao {
    @Query("SELECT * FROM transfer_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TransferSessionEntity>>

    @Query("SELECT * FROM transfer_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): TransferSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TransferSessionEntity)

    @Update
    suspend fun updateSession(session: TransferSessionEntity)

    @Query("DELETE FROM transfer_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)
    
    @Query("DELETE FROM transfer_sessions")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<TransferFileEntity>)

    @Query("SELECT * FROM transfer_files WHERE sessionId = :sessionId")
    suspend fun getFilesForSession(sessionId: String): List<TransferFileEntity>
    
    @Update
    suspend fun updateFile(file: TransferFileEntity)
}
