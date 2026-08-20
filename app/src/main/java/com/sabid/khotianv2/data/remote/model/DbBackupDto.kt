package com.sabid.khotianv2.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbBackupDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("file_path") val filePath: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("version_code") val versionCode: Int
)
