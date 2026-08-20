package com.sabid.khotianv2.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.sabid.khotianv2.BuildConfig
import com.sabid.khotianv2.data.remote.model.AppReleaseDto
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    suspend fun checkForUpdates(): AppReleaseDto? = withContext(Dispatchers.IO) {
        try {
            val release = supabaseClient.postgrest.from("app_releases")
                .select()
                .decodeSingle<AppReleaseDto>()
            
            if (release.versionCode > BuildConfig.VERSION_CODE) {
                release
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadAndInstallApk(release: AppReleaseDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bucket = supabaseClient.storage.from("releases")
            val bytes = bucket.downloadPublic(release.apkPath)
            
            val apkFile = File(context.cacheDir, "update.apk")
            apkFile.writeBytes(bytes)
            
            installApk(apkFile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
