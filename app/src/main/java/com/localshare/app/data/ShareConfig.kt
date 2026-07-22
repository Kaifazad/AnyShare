package com.localshare.app.data

/**
 * Holds the sharing configuration: a simple queue of explicitly selected files.
 */
data class ShareConfig(
    val sharedFiles: List<SharedFile> = emptyList()
) {
    fun addFiles(files: List<SharedFile>): ShareConfig {
        val currentIds = sharedFiles.map { it.id }.toSet()
        val newFiles = files.filter { it.id !in currentIds }
        return copy(sharedFiles = sharedFiles + newFiles)
    }

    fun removeFile(id: Long): ShareConfig {
        return copy(sharedFiles = sharedFiles.filter { it.id != id })
    }

    fun removeFiles(ids: Set<Long>): ShareConfig {
        return copy(sharedFiles = sharedFiles.filter { it.id !in ids })
    }

    fun clear(): ShareConfig {
        return copy(sharedFiles = emptyList())
    }

    val hasAnythingShared: Boolean
        get() = sharedFiles.isNotEmpty()
}
