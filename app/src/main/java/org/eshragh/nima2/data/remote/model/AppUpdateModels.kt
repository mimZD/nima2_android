package org.eshragh.nima2.data.remote.model

import com.google.gson.annotations.SerializedName

data class CheckUpdateRequest(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("currentVersionCode") val currentVersionCode: Int,
    @SerializedName("deviceModel") val deviceModel: String? = null,
    @SerializedName("androidVersion") val androidVersion: String? = null,
    @SerializedName("userId") val userId: String? = null
)

data class CheckUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: UpdateData?
)

data class UpdateData(
    @SerializedName("updateAvailable") val updateAvailable: Boolean,
    @SerializedName("isForceUpdate") val isForceUpdate: Boolean,
    @SerializedName("latestVersion") val latestVersion: LatestVersionInfo?
)

data class LatestVersionInfo(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("releaseNotes") val releaseNotes: String?,
    @SerializedName("downloadUrl") val downloadUrl: String,
    @SerializedName("fileSize") val fileSize: Long,
    @SerializedName("fileMd5") val fileMd5: String?
)
