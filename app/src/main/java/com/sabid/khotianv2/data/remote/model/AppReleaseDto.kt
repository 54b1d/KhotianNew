package com.sabid.khotianv2.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppReleaseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("version_name") val versionName: String,
    @SerialName("version_code") val versionCode: Int,
    @SerialName("apk_path") val apkPath: String,
    @SerialName("release_notes") val releaseNotes: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
