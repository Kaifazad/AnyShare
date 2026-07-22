package com.localshare.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Repository for resolving Android URIs into SharedFile objects.
 */
class FileRepository(private val context: Context) {

    fun resolveUris(uris: List<Uri>): List<SharedFile> {
        return uris.mapNotNull { resolveUri(it) }
    }

    private fun resolveUri(uri: Uri): SharedFile? {
        var name = "Unknown File"
        var size = 0L
        var mimeType = "application/octet-stream"

        if (uri.scheme == "content") {
            // Standard document query
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dnIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val szIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (dnIdx != -1) cursor.getString(dnIdx)?.let { if (it.isNotEmpty()) name = it }
                    if (szIdx != -1) size = cursor.getLong(szIdx)
                }
            }

            // If name is still numeric (photo picker issue), resolve via MediaStore
            if (name.matches(Regex("^\\d+$"))) {
                val mediaId = name
                name = resolveNameFromMediaStore(mediaId) ?: name
            }

            // If still numeric, try last path segment
            if (name.matches(Regex("^\\d+$"))) {
                uri.lastPathSegment?.let { if (it.isNotEmpty() && !it.matches(Regex("^\\d+$"))) name = it }
            }

            val resolverMime = context.contentResolver.getType(uri)
            mimeType = if (!resolverMime.isNullOrBlank() && resolverMime != "application/octet-stream") {
                resolverMime
            } else {
                getMimeType(name)
            }
        } else if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            name = file.name
            size = file.length()
            mimeType = getMimeType(name)
        }

        // Generate a stable ID based on URI string
        val id = uri.toString().hashCode().toLong()

        return SharedFile(
            id = id,
            name = name,
            path = uri.toString(),
            uri = uri,
            size = size,
            mimeType = mimeType,
            category = FileCategory.fromMimeType(mimeType),
            lastModified = System.currentTimeMillis()
        )
    }

    fun resolveFolder(treeUri: Uri): SharedFile? {
        val documentFile = DocumentFile.fromTreeUri(context, treeUri) ?: return null

        val id = treeUri.toString().hashCode().toLong()
        val name = documentFile.name ?: "Folder"
        val size = calculateFolderSize(documentFile)

        return SharedFile(
            id = id,
            name = name,
            path = treeUri.toString(),
            uri = treeUri,
            size = size,
            mimeType = "application/x-directory",
            category = FileCategory.CUSTOM_FOLDERS,
            lastModified = System.currentTimeMillis()
        )
    }

    private fun calculateFolderSize(documentFile: DocumentFile): Long {
        var totalSize = 0L
        documentFile.listFiles().forEach { child ->
            if (child.isDirectory) {
                totalSize += calculateFolderSize(child)
            } else {
                totalSize += child.length()
            }
        }
        return totalSize
    }

    fun getInstalledApps(): List<SharedFile> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val apps = mutableListOf<SharedFile>()

        for (appInfo in packages) {
            // Filter out system apps unless they are updated
            if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || 
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                
                val file = File(appInfo.sourceDir)
                if (file.exists()) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    apps.add(
                    SharedFile(
                        id = file.absolutePath.hashCode().toLong(),
                            name = "$appName.apk",
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
        return apps.sortedBy { it.name.lowercase() }
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val fromMap = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (!fromMap.isNullOrBlank() && fromMap != "application/octet-stream") {
            return fromMap
        }
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "heic", "heif" -> "image/heic"
            "avif" -> "image/avif"
            "ico" -> "image/x-icon"
            
            "mp4", "m4v" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "flv" -> "video/x-flv"
            "ts" -> "video/mp2t"
            "wmv" -> "video/x-ms-wmv"
            
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "flac" -> "audio/flac"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "wma" -> "audio/x-ms-wma"

            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "rar" -> "application/vnd.rar"
            "7z" -> "application/x-7z-compressed"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "txt", "log", "md" -> "text/plain"
            
            else -> "application/octet-stream"
        }
    }

    private fun resolveNameFromMediaStore(mediaId: String): String? {
        val id = mediaId.toLongOrNull() ?: return null
        val tables = arrayOf(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )
        for (tableUri in tables) {
            try {
                val itemUri = android.content.ContentUris.withAppendedId(tableUri, id)
                context.contentResolver.query(itemUri, arrayOf("_display_name", "_data"), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val dnIdx = cursor.getColumnIndex("_display_name")
                        val dataIdx = cursor.getColumnIndex("_data")
                        val displayName = if (dnIdx != -1) cursor.getString(dnIdx) else null
                        val dataPath = if (dataIdx != -1) cursor.getString(dataIdx) else null
                        // Prefer _data path extraction over _display_name
                        if (!dataPath.isNullOrEmpty()) {
                            val fileName = File(dataPath).name
                            if (fileName.isNotEmpty() && !fileName.matches(Regex("^\\d+$"))) return fileName
                        }
                        if (!displayName.isNullOrEmpty() && !displayName.matches(Regex("^\\d+$"))) return displayName
                    }
                }
            } catch (_: Exception) { }
        }
        return null
    }
}
