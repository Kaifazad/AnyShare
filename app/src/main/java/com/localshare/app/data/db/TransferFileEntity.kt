package com.localshare.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "transfer_files",
    foreignKeys = [
        ForeignKey(
            entity = TransferSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class TransferFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val fileId: String,
    val fileName: String,
    val size: Long,
    val fileType: String,
    val uriString: String?, // Null for received files initially
    val isCompleted: Boolean = false
)
