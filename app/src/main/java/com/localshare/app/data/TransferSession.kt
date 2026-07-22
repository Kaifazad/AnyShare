package com.localshare.app.data

/**
 * Represents an incoming or outgoing phone-to-phone transfer session.
 */
data class TransferSession(
    val sessionId: String,
    val senderName: String,
    val senderIp: String,
    val files: List<FileInfo>,
    val totalSize: Long,
    val status: SessionStatus = SessionStatus.PENDING,
    val transferredBytes: Long = 0,
    val speedBytesPerSecond: Long = 0,
    val etaSeconds: Long = 0,
    val transferType: TransferType = TransferType.RECEIVE,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0
)

data class FileInfo(
    val id: String,
    val fileName: String,
    val size: Long,
    val fileType: String,
    val sha256: String? = null
)

enum class TransferType {
    SEND,
    RECEIVE
}

enum class SessionStatus {
    PENDING,    // Waiting for accept/reject
    ACTIVE,     // Transfer in progress
    PAUSED,     // Transfer paused
    COMPLETED,  // All files transferred
    FAILED,     // Transfer failed
    CANCELLED,  // Cancelled by user
    REJECTED    // Rejected by receiver
}
