package org.eshragh.nima2.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import org.eshragh.nima2.data.remote.model.UpdateData
import org.eshragh.nima2.util.UpdateDownloadState
import org.eshragh.nima2.util.toPersianDigits
import java.io.File
import java.util.Locale

@Composable
fun AppUpdateDialog(
    updateData: UpdateData,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val versionInfo = updateData.latestVersion ?: return
    val isForce = updateData.isForceUpdate

    // Auto trigger installation if ready
    LaunchedEffect(downloadState) {
        if (downloadState is UpdateDownloadState.ReadyToInstall) {
            onInstallApk(downloadState.apkFile)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = {
                if (!isForce && downloadState !is UpdateDownloadState.Downloading) {
                    onDismiss()
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !isForce && downloadState !is UpdateDownloadState.Downloading,
                dismissOnClickOutside = !isForce && downloadState !is UpdateDownloadState.Downloading
            ),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isForce) "به‌روزرسانی اجباری" else "نسخه جدید موجود است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نسخه " + versionInfo.versionName.toPersianDigits() + " (" + versionInfo.versionCode.toString().toPersianDigits() + ")",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // File size display
                    if (versionInfo.fileSize > 0) {
                        val sizeMb = versionInfo.fileSize / (1024f * 1024f)
                        val sizeText = String.format(Locale.US, "%.1f مگابایت", sizeMb).toPersianDigits()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "حجم فایل به‌روزرسانی:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = sizeText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Release notes
                    if (!versionInfo.releaseNotes.isNullOrBlank()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "تغییرات این نسخه:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = versionInfo.releaseNotes.toPersianDigits(),
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Download State Content
                    when (downloadState) {
                        is UpdateDownloadState.Idle -> {
                            if (isForce) {
                                Text(
                                    text = "برای ادامه استفاده از برنامه، باید آن را به آخرین نسخه به‌روزرسانی کنید.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        is UpdateDownloadState.Downloading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    progress = { downloadState.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val percent = (downloadState.progress * 100).toInt()
                                val downloadedMb = downloadState.downloadedBytes / (1024f * 1024f)
                                val totalMb = downloadState.totalBytes / (1024f * 1024f)
                                val progressText = String.format(
                                    Locale.US,
                                    "%d٪ (%.1f / %.1f مگابایت)",
                                    percent,
                                    downloadedMb,
                                    totalMb
                                ).toPersianDigits()

                                Text(
                                    text = progressText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is UpdateDownloadState.VerifyingMd5 -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "در حال تایید سلامت فایل...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is UpdateDownloadState.ReadyToInstall -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "فایل آماده نصب است",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is UpdateDownloadState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = downloadState.message,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Button(
                            onClick = onStartDownload,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دانلود و نصب نسـخه جدید", fontWeight = FontWeight.Bold)
                        }
                    }
                    is UpdateDownloadState.Downloading, is UpdateDownloadState.VerifyingMd5 -> {
                        // Button disabled during active download
                    }
                    is UpdateDownloadState.ReadyToInstall -> {
                        Button(
                            onClick = { onInstallApk(downloadState.apkFile) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("نصب فایل دانلود شده", fontWeight = FontWeight.Bold)
                        }
                    }
                    is UpdateDownloadState.Error -> {
                        Button(
                            onClick = onStartDownload,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تلاش مجدد", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (!isForce && downloadState !is UpdateDownloadState.Downloading) {
                    TextButton(onClick = onDismiss) {
                        Text("بعداً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}
