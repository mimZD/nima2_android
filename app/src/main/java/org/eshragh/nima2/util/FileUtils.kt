package org.eshragh.nima2.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object FileUtils {
    fun getFileIcon(fileName: String, mimeType: String?): ImageVector {
        val name = fileName.lowercase()
        val mime = mimeType?.lowercase() ?: ""
        
        return when {
            name.endsWith(".pdf") || mime.contains("pdf") -> Icons.Default.PictureAsPdf
            name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") || mime.contains("zip") || mime.contains("compressed") -> Icons.Default.FolderZip
            name.endsWith(".doc") || name.endsWith(".docx") || mime.contains("word") || mime.contains("officedocument.word") -> Icons.Default.Description
            name.endsWith(".xls") || name.endsWith(".xlsx") || mime.contains("excel") || mime.contains("officedocument.spreadsheet") -> Icons.Default.TableChart
            name.endsWith(".ppt") || name.endsWith(".pptx") || mime.contains("powerpoint") || mime.contains("officedocument.presentation") -> Icons.Default.PresentToAll
            name.endsWith(".txt") || mime.contains("text/plain") -> Icons.Default.Article
            mime.contains("audio") -> Icons.Default.AudioFile
            mime.contains("video") -> Icons.Default.VideoFile
            else -> Icons.Default.InsertDriveFile
        }
    }
}
