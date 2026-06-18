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
            val docFile = DocumentFile.fromSingleUri(context, uri)
            if (docFile != null && docFile.name != null) {
                name = docFile.name!!
                size = docFile.length()
            } else {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: "Unknown"
                        }
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex != -1) {
                            size = cursor.getLong(sizeIndex)
                        }
                    }
                }
            }
            mimeType = context.contentResolver.getType(uri) ?: mimeType
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
            path = uri.toString(), // We store the URI string in path for reference
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
        
        return SharedFile(
            id = id,
            name = name,
            path = treeUri.toString(),
            uri = treeUri,
            size = 0L, // Folders don't have a fixed size
            mimeType = "application/x-directory",
            category = FileCategory.CUSTOM_FOLDERS,
            lastModified = System.currentTimeMillis()
        )
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
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
    }
}
