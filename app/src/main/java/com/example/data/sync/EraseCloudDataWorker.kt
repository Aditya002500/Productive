package com.example.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase

/**
 * Durable, network-constrained follow-up to local erase-all: deletes the signed-in
 * user's Firestore documents and Storage files. Runs as a retryable background job
 * rather than an inline best-effort call so a failed attempt (e.g. offline) can't
 * leave stale remote data that a later sync would otherwise resurrect locally.
 */
class EraseCloudDataWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val KEY_UID = "uid"
    }

    override suspend fun doWork(): Result {
        val uid = inputData.getString(KEY_UID) ?: return Result.failure()
        return try {
            val dao = AppDatabase.getDatabase(applicationContext).appDao()
            CloudSyncRepository(dao, applicationContext).eraseRemoteUserData(uid)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
