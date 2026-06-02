package com.localshare.app.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for querying device files via MediaStore.
 */
class FileRepository(private val context: Context) {

    /**
     * Query all files for a given category from MediaStore.
     */
    suspend fun getFilesForCategory(
        category: FileCategory, 
        customFolderUris: Set<String> = emptySet()
    ): List<SharedFile> = withContext(Dispatchers.IO) {
        when (category) {
            FileCategory.VIDEOS -> queryMediaStore(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                category
            )
            FileCategory.PHOTOS -> queryMediaStore(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                category
            )
            FileCategory.AUDIO -> queryMediaStore(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                category
            )
            FileCategory.DOCUMENTS -> queryDocuments()
            FileCategory.DOWNLOADS -> queryDownloads()
            FileCategory.APPS -> queryApps()
            FileCategory.CUSTOM_FOLDERS -> queryCustomFolders(customFolderUris)
        }
    }

    /**
     * Get all files across all categories.
     */
    suspend fun getAllFiles(customFolderUris: Set<String> = emptySet()): Map<FileCategory, List<SharedFile>> = withContext(Dispatchers.IO) {
        FileCategory.entries.associateWith { category ->
            try {
                getFilesForCategory(category, customFolderUris)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Get files that should be shared based on the config.
     */
    suspend fun getSharedFiles(config: ShareConfig): List<SharedFile> = withContext(Dispatchers.IO) {
        val result = mutableListOf<SharedFile>()

        // Add files from enabled categories
        for (category in FileCategory.entries) {
            if (config.isCategoryEnabled(category)) {
                result.addAll(getFilesForCategory(category, config.customFolderUris))
            }
        }

        // Always add custom folders if they exist (they are implicitly shared)
        if (config.customFolderUris.isNotEmpty() && !config.isCategoryEnabled(FileCategory.CUSTOM_FOLDERS)) {
            result.addAll(getFilesForCategory(FileCategory.CUSTOM_FOLDERS, config.customFolderUris))
        }

        // Add individually selected files (if not already included via category)
        if (config.selectedFileIds.isNotEmpty()) {
            val allFiles = FileCategory.entries.flatMap { getFilesForCategory(it, config.customFolderUris) }
            val categoryFileIds = result.map { it.id }.toSet()
            val individualFiles = allFiles.filter {
                it.id in config.selectedFileIds && it.id !in categoryFileIds
            }
            result.addAll(individualFiles)
        }

        result.sortedByDescending { it.lastModified }
    }

    /**
     * Find a specific file by its ID across all categories.
     */
    suspend fun findFileById(fileId: Long, customFolderUris: Set<String> = emptySet()): SharedFile? = withContext(Dispatchers.IO) {
        for (category in FileCategory.entries) {
            val files = getFilesForCategory(category, customFolderUris)
            val file = files.find { it.id == fileId }
            if (file != null) return@withContext file
        }
        null
    }

    private fun queryMediaStore(
        contentUri: Uri,
        category: FileCategory
    ): List<SharedFile> {
        val files = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATA
        )

        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            contentUri,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeColumn) ?: "application/octet-stream"
                val dateModified = cursor.getLong(dateColumn) * 1000
                val data = cursor.getString(dataColumn) ?: ""
                val uri = ContentUris.withAppendedId(contentUri, id)

                files.add(
                    SharedFile(
                        id = id,
                        name = name,
                        path = data,
                        uri = uri,
                        size = size,
                        mimeType = mimeType,
                        category = category,
                        lastModified = dateModified
                    )
                )
            }
        }

        return files
    }

    private fun queryDocuments(): List<SharedFile> {
        val files = mutableListOf<SharedFile>()

        // Query from MediaStore.Files for document types
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATA
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?, ?)"
        val selectionArgs = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "application/zip"
        )

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeColumn) ?: "application/octet-stream"
                val dateModified = cursor.getLong(dateColumn) * 1000
                val data = cursor.getString(dataColumn) ?: ""
                val uri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"), id
                )

                files.add(
                    SharedFile(
                        id = id,
                        name = name,
                        path = data,
                        uri = uri,
                        size = size,
                        mimeType = mimeType,
                        category = FileCategory.DOCUMENTS,
                        lastModified = dateModified
                    )
                )
            }
        }

        return files
    }

    private fun queryDownloads(): List<SharedFile> {
        val files = mutableListOf<SharedFile>()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        if (downloadsDir.exists() && downloadsDir.isDirectory) {
            downloadsDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val mimeType = getMimeType(file.name)
                    files.add(
                        SharedFile(
                            id = file.absolutePath.hashCode().toLong(),
                            name = file.name,
                            path = file.absolutePath,
                            uri = Uri.fromFile(file),
                            size = file.length(),
                            mimeType = mimeType,
                            category = FileCategory.DOWNLOADS,
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }

        return files.sortedByDescending { it.lastModified }
    }

    private fun queryApps(): List<SharedFile> {
        val files = mutableListOf<SharedFile>()
        val pm = context.packageManager
        
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in apps) {
            // Filter out system apps to avoid clutter
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            
            if (!isSystemApp || isUpdatedSystemApp) {
                val file = File(appInfo.sourceDir)
                if (file.exists()) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    // append .apk so that it downloads correctly
                    val fileName = if (appName.endsWith(".apk", ignoreCase = true)) appName else "$appName.apk"
                    
                    files.add(
                        SharedFile(
                            id = file.absolutePath.hashCode().toLong(),
                            name = fileName,
                            path = file.absolutePath,
                            uri = Uri.fromFile(file),
                            size = file.length(),
                            mimeType = "application/vnd.android.package-archive",
                            category = FileCategory.APPS,
                            lastModified = file.lastModified()
                        )
                    )
                }
            }
        }
        
        return files.sortedBy { it.name.lowercase() }
    }

    private fun getMimeType(filename: String): String {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "mp4", "mkv", "avi", "mov", "webm" -> "video/$extension"
            "mp3", "wav", "flac", "aac", "ogg" -> "audio/$extension"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }

    private fun queryCustomFolders(customFolderUris: Set<String>): List<SharedFile> {
        val files = mutableListOf<SharedFile>()
        
        for (uriString in customFolderUris) {
            try {
                val treeUri = Uri.parse(uriString)
                val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                
                if (documentFile != null && documentFile.isDirectory) {
                    val folderName = documentFile.name ?: "Folder"
                    documentFile.listFiles().forEach { childFile ->
                        if (childFile.isFile) {
                            val mimeType = childFile.type ?: getMimeType(childFile.name ?: "")
                            files.add(
                                SharedFile(
                                    id = childFile.uri.toString().hashCode().toLong(),
                                    name = "${folderName}/${childFile.name ?: "Unknown"}",
                                    path = childFile.uri.toString(),
                                    uri = childFile.uri,
                                    size = childFile.length(),
                                    mimeType = mimeType,
                                    category = FileCategory.CUSTOM_FOLDERS,
                                    lastModified = childFile.lastModified()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore inaccessible URIs
            }
        }
        
        return files.sortedByDescending { it.lastModified }
    }
}
