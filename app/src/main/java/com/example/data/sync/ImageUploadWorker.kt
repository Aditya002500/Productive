package com.example.data.sync

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Uploads a locally-cached image (copied out of the Photo Picker's content:// URI at
 * pick time, since that grant isn't guaranteed to survive process death) to Firebase
 * Storage, then flips the owning row's URL field to the resulting https download URL
 * so the image is visible from any device.
 */
class ImageUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val KEY_ENTITY_TYPE = "entity_type" // "profile", "note", "capture"
        const val KEY_ENTITY_SYNC_ID = "entity_sync_id" // for "profile" this is the uid
        const val KEY_LOCAL_FILE_PATH = "local_file_path"
        const val KEY_REMOTE_STORAGE_PATH = "remote_storage_path"
        const val KEY_IMAGE_INDEX = "image_index" // for notes with multiple images; -1 when not applicable
    }

    override suspend fun doWork(): Result {
        val localPath = inputData.getString(KEY_LOCAL_FILE_PATH) ?: return Result.failure()
        val remotePath = inputData.getString(KEY_REMOTE_STORAGE_PATH) ?: return Result.failure()
        val entityType = inputData.getString(KEY_ENTITY_TYPE) ?: return Result.failure()
        val syncId = inputData.getString(KEY_ENTITY_SYNC_ID) ?: return Result.failure()
        val imageIndex = inputData.getInt(KEY_IMAGE_INDEX, -1)

        val file = File(localPath)
        if (!file.exists()) return Result.failure()

        return try {
            val storageRef = FirebaseStorage.getInstance().reference.child(remotePath)
            storageRef.putFile(Uri.fromFile(file)).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val dao = AppDatabase.getDatabase(applicationContext).appDao()
            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            when (entityType) {
                "profile" -> {
                    firestore.collection("users").document(syncId)
                        .set(mapOf("photoUrl" to downloadUrl, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                        .await()
                }
                "note" -> {
                    val note = dao.getNoteBySyncId(syncId) ?: return Result.success()
                    val urls = note.referenceImageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                    if (imageIndex in urls.indices) urls[imageIndex] = downloadUrl else urls.add(downloadUrl)
                    val updated = note.copy(referenceImageUrls = urls.joinToString(","), updatedAt = System.currentTimeMillis())
                    dao.insertNote(updated)
                    if (uid != null) {
                        firestore.collection("users").document(uid).collection("notes").document(updated.syncId)
                            .set(noteToFirestoreMap(updated), SetOptions.merge()).await()
                    }
                }
                "capture" -> {
                    val capture = dao.getCaptureBySyncId(syncId) ?: return Result.success()
                    val updated = capture.copy(imageUrl = downloadUrl, updatedAt = System.currentTimeMillis())
                    dao.updateCapture(updated)
                    if (uid != null) {
                        firestore.collection("users").document(uid).collection("captures").document(updated.syncId)
                            .set(captureToFirestoreMap(updated), SetOptions.merge()).await()
                    }
                }
            }
            file.delete()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
