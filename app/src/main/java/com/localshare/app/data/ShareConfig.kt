package com.localshare.app.data

/**
 * Holds the sharing configuration: which categories are enabled
 * and which individual files are selected.
 */
data class ShareConfig(
    val categoryToggles: Map<FileCategory, Boolean> = FileCategory.entries.associateWith { false },
    val selectedFileIds: Set<Long> = emptySet(),
    val customFolderUris: Set<String> = emptySet()
) {
    fun isCategoryEnabled(category: FileCategory): Boolean {
        return categoryToggles[category] ?: false
    }

    fun toggleCategory(category: FileCategory): ShareConfig {
        val current = isCategoryEnabled(category)
        return copy(
            categoryToggles = categoryToggles + (category to !current)
        )
    }

    fun addFile(fileId: Long): ShareConfig {
        return copy(selectedFileIds = selectedFileIds + fileId)
    }

    fun removeFile(fileId: Long): ShareConfig {
        return copy(selectedFileIds = selectedFileIds - fileId)
    }

    fun toggleFile(fileId: Long): ShareConfig {
        return if (fileId in selectedFileIds) removeFile(fileId) else addFile(fileId)
    }

    fun isFileSelected(fileId: Long): Boolean {
        return fileId in selectedFileIds
    }

    fun addCustomFolder(uriString: String): ShareConfig {
        return copy(customFolderUris = customFolderUris + uriString)
    }

    fun removeCustomFolder(uriString: String): ShareConfig {
        return copy(customFolderUris = customFolderUris - uriString)
    }

    val hasAnythingShared: Boolean
        get() = categoryToggles.any { it.value } || selectedFileIds.isNotEmpty() || customFolderUris.isNotEmpty()
}
