package com.localshare.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.localshare.app.data.SessionStatus
import com.localshare.app.data.TransferType

@Entity(tableName = "transfer_sessions")
data class TransferSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val senderName: String,
    val senderIp: String,
    val totalFiles: Int,
    val totalSize: Long,
    val transferredBytes: Long,
    val status: SessionStatus,
    val transferType: TransferType,
    val startTime: Long,
    val endTime: Long
)
