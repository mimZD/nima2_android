package org.eshragh.nima2.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eshragh.nima2.data.remote.UpdateRetrofitClient
import org.eshragh.nima2.data.remote.model.CheckUpdateRequest
import org.eshragh.nima2.data.remote.model.LatestVersionInfo
import org.eshragh.nima2.data.remote.model.UpdateData
import java.io.File
import java.security.MessageDigest

sealed class UpdateDownloadState {
    object Idle : UpdateDownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : UpdateDownloadState()
    object VerifyingMd5 : UpdateDownloadState()
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState()
    data class Error(val message: String) : UpdateDownloadState()
}

class AppUpdateManager(private val context: Context) {

    fun getCurrentVersionCode(): Int {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (_: Exception) {
            5 // Default fallback
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.41"
        } catch (e: Exception) {
            "0.41"
        }
    }

    suspend fun checkUpdate(userId: String? = null): Result<UpdateData?> = withContext(Dispatchers.IO) {
        try {
            val request = CheckUpdateRequest(
                packageName = context.packageName,
                currentVersionCode = getCurrentVersionCode(),
                deviceModel = Build.MODEL,
                androidVersion = "SDK " + Build.VERSION.SDK_INT,
                userId = userId
            )

            val response = UpdateRetrofitClient.api.checkUpdate(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("خطا در پاسخ سرور آپدیت"))
                }
            } else {
                Result.failure(Exception("خطا در ارتباط با سرور آپدیت (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndPrepareApk(
        versionInfo: LatestVersionInfo,
        onStateChange: (UpdateDownloadState) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onStateChange(UpdateDownloadState.Downloading(0f, 0L, versionInfo.fileSize))

            val response = UpdateRetrofitClient.api.downloadApk(versionInfo.downloadUrl)
            val body = response.body() ?: throw Exception("بدنه پاسخ دانلود خالی است")

            val updateDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
            val apkFile = File(updateDir, "nima2_v${versionInfo.versionCode}.apk")

            val totalBytes = if (versionInfo.fileSize > 0) versionInfo.fileSize else body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes) else 0f
                        onStateChange(UpdateDownloadState.Downloading(progress, downloadedBytes, totalBytes))
                    }
                }
            }

            // Verify MD5
            onStateChange(UpdateDownloadState.VerifyingMd5)
            val expectedMd5 = versionInfo.fileMd5?.trim()
            if (!expectedMd5.isNullOrEmpty()) {
                val computedMd5 = calculateMd5(apkFile)
                if (!computedMd5.equals(expectedMd5, ignoreCase = true)) {
                    apkFile.delete()
                    onStateChange(UpdateDownloadState.Error("هش سلامت فایل (MD5) مطابقت ندارد. دانلود خراب شده است."))
                    return@withContext
                }
            }

            onStateChange(UpdateDownloadState.ReadyToInstall(apkFile))
        } catch (e: Exception) {
            onStateChange(UpdateDownloadState.Error("خطا در دانلود آپدیت: ${e.localizedMessage}"))
        }
    }

    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        if (!canInstallPackages()) {
            openInstallPermissionSettings()
            return false
        }

        return try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            android.util.Log.e("NIMA2_UPDATE", "Error launching APK install intent: ${e.localizedMessage}")
            false
        }
    }

    private fun calculateMd5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
