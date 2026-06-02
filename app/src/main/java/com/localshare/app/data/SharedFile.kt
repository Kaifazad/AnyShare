package com.localshare.app.data

import android.net.Uri

/**
 * Represents a file that can be shared via the local server.
 */
data class SharedFile(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val size: Long,
    val mimeType: String,
    val category: FileCategory,
    val lastModified: Long,
    val isSelected: Boolean = false
) {
    val formattedSize: String
        get() {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.1f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                kb >= 1.0 -> String.format("%.1f KB", kb)
                else -> "$size B"
            }
        }

    val isStreamable: Boolean
        get() = mimeType.startsWith("video/") ||
                mimeType.startsWith("audio/") ||
                mimeType.startsWith("image/")

    val typeIcon: String
        get() = when {
            mimeType.startsWith("video/") -> "video"
            mimeType.startsWith("image/") -> "image"
            mimeType.startsWith("audio/") -> "audio"
            mimeType.startsWith("text/") -> "document"
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("tar") -> "archive"
            mimeType.contains("apk") -> "android"
            else -> "file"
        }
}

/**
 * Categories for file organization.
 */
enum class FileCategory(val displayName: String) {
    VIDEOS("Videos"),
    PHOTOS("Photos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    DOWNLOADS("Downloads"),
    APPS("Apps"),
    CUSTOM_FOLDERS("Folders");

    companion object {
        fun fromMimeType(mimeType: String): FileCategory {
            return when {
                mimeType.startsWith("video/") -> VIDEOS
                mimeType.startsWith("image/") -> PHOTOS
                mimeType.startsWith("audio/") -> AUDIO
                mimeType == "application/vnd.android.package-archive" -> APPS
                else -> DOCUMENTS
            }
        }
    }
}
