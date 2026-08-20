package com.sabid.khotianv2.data.local

import android.content.Context
import com.sabid.khotianv2.BuildConfig
import com.sabid.khotianv2.data.remote.model.DbBackupDto
import com.sabid.khotianv2.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.work.*
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository
) {
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CloudSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    suspend fun performSync(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.getSession() ?: return@withContext Result.failure(Exception("Not logged in"))
        
        try {
            val dbFile = context.getDatabasePath("khotian_new.db")
            if (!dbFile.exists()) return@withContext Result.failure(Exception("Database file not found"))
            
            val backupFile = File(context.cacheDir, "backup_${System.currentTimeMillis()}.db")
            dbFile.copyTo(backupFile, overwrite = true)
            
            val storagePath = "db-backups/$userId/${backupFile.name}"
            val bucket = supabaseClient.storage.from("backups")
            
            val bytes = backupFile.readBytes()
            bucket.upload(storagePath, bytes)
            
            val backupDto = DbBackupDto(
                userId = userId,
                filePath = storagePath,
                versionCode = BuildConfig.VERSION_CODE
            )
            
            supabaseClient.postgrest.from("db_backups").insert(backupDto)
            
            backupFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
