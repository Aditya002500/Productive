package com.example.data.sync

import android.content.Context
import com.example.data.AppDao
import com.example.data.CaptureEntity
import com.example.data.EventEntity
import com.example.data.HabitEntity
import com.example.data.HabitScheduler
import com.example.data.NoteEntity
import com.example.data.SubTaskEntity
import com.example.data.TaskEntity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ProfileSnapshot(val displayName: String, val email: String, val photoUrl: String?, val updatedAt: Long)

/**
 * Mirrors the local Room database to Firestore under users/{uid}/... so the same
 * signed-in account sees the same data on every device. Room stays the single
 * source of truth for reads (widget, notifications, offline use) — this class only
 * pushes local writes out and pulls remote changes back in, last-write-wins by
 * comparing [TaskEntity.updatedAt]-style timestamps.
 */
class CloudSyncRepository(
    private val appDao: AppDao,
    private val appContext: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val listeners = mutableListOf<ListenerRegistration>()
    private var activeUid: String? = null

    private val _remoteProfile = MutableStateFlow<ProfileSnapshot?>(null)
    val remoteProfile: StateFlow<ProfileSnapshot?> = _remoteProfile.asStateFlow()

    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun collection(uid: String, name: String) = userDoc(uid).collection(name)

    fun stopSync() {
        listeners.forEach { it.remove() }
        listeners.clear()
        activeUid = null
        _remoteProfile.value = null
    }

    fun startSyncForUser(uid: String, scope: CoroutineScope) {
        if (activeUid == uid) return
        stopSync()
        activeUid = uid
        scope.launch(Dispatchers.IO) {
            val regs = listOf(
                reconcileTasks(uid),
                reconcileNotes(uid),
                reconcileCaptures(uid),
                reconcileEvents(uid),
                reconcileHabits(uid),
                attachProfileListener(uid)
            )
            if (activeUid == uid) listeners.addAll(regs) else regs.forEach { it.remove() }
        }
    }

    // ---- generic reconcile-then-listen engine ----

    private suspend fun <T : Any> reconcileAndListen(
        uid: String,
        name: String,
        localAll: suspend () -> List<T>,
        syncIdOf: (T) -> String,
        toMap: suspend (T) -> Map<String, Any?>,
        applyRemote: suspend (Map<String, Any?>) -> Unit,
        deleteLocalBySyncId: suspend (String) -> Unit
    ): ListenerRegistration {
        val coll = collection(uid, name)
        val remoteSnap = coll.get().await()
        val remoteIds = remoteSnap.documents.map { it.id }.toSet()

        // Push rows that exist only locally (offline-created, or created before sync existed).
        for (local in localAll()) {
            val sid = syncIdOf(local)
            if (sid !in remoteIds) {
                coll.document(sid).set(toMap(local), SetOptions.merge()).await()
            }
        }
        // Pull every remote row through the same LWW-gated path the live listener uses;
        // applyRemote itself re-asserts a row up to Firestore when the local copy is newer.
        for (doc in remoteSnap.documents) {
            applyRemote(doc.data ?: emptyMap())
        }

        return coll.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            for (change in snapshot.documentChanges) {
                if (change.document.metadata.hasPendingWrites()) continue // ignore this device's own echo
                when (change.type) {
                    DocumentChange.Type.REMOVED ->
                        CoroutineScope(Dispatchers.IO).launch { deleteLocalBySyncId(change.document.id) }
                    else ->
                        CoroutineScope(Dispatchers.IO).launch { applyRemote(change.document.data) }
                }
            }
        }
    }

    // ---- Tasks (+ embedded sub-tasks) ----

    private suspend fun reconcileTasks(uid: String): ListenerRegistration = reconcileAndListen(
        uid = uid,
        name = "tasks",
        localAll = { appDao.getAllTasksFlow().first() },
        syncIdOf = { it.syncId },
        toMap = { taskToFirestoreMap(it, appDao.getSubTasksForTask(it.id)) },
        applyRemote = { applyRemoteTask(it) },
        deleteLocalBySyncId = { deleteLocalTaskBySyncId(it) }
    )

    suspend fun pushTask(task: TaskEntity, subTasks: List<SubTaskEntity>) {
        val uid = activeUid ?: return
        collection(uid, "tasks").document(task.syncId).set(taskToFirestoreMap(task, subTasks), SetOptions.merge()).await()
    }

    private suspend fun applyRemoteTask(data: Map<String, Any?>) {
        val syncId = data["syncId"] as? String ?: return
        val remoteUpdatedAt = (data["updatedAt"] as? Long) ?: 0L
        val existing = appDao.getTaskBySyncId(syncId)
        if (existing != null && existing.updatedAt >= remoteUpdatedAt) {
            if (existing.updatedAt > remoteUpdatedAt) pushTask(existing, appDao.getSubTasksForTask(existing.id))
            return
        }

        val task = TaskEntity(
            id = existing?.id ?: 0,
            title = data["title"] as? String ?: "",
            isCompleted = data["isCompleted"] as? Boolean ?: false,
            priority = data["priority"] as? String ?: "Medium",
            dueDate = data["dueDate"] as? String ?: "",
            dueTime = data["dueTime"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            syncId = syncId,
            updatedAt = remoteUpdatedAt
        )
        val localTaskId = if (existing != null) {
            appDao.updateTask(task)
            existing.id
        } else {
            appDao.insertTask(task).toInt()
        }

        @Suppress("UNCHECKED_CAST")
        val remoteSubtasks = (data["subtasks"] as? List<Map<String, Any?>>) ?: emptyList()
        appDao.deleteSubTasksForTask(localTaskId)
        remoteSubtasks.forEach { st ->
            appDao.insertSubTask(
                SubTaskEntity(
                    taskId = localTaskId,
                    title = st["title"] as? String ?: "",
                    isCompleted = st["isCompleted"] as? Boolean ?: false,
                    syncId = st["syncId"] as? String ?: UUID.randomUUID().toString(),
                    updatedAt = (st["updatedAt"] as? Long) ?: remoteUpdatedAt
                )
            )
        }
    }

    private suspend fun deleteLocalTaskBySyncId(syncId: String) {
        val existing = appDao.getTaskBySyncId(syncId) ?: return
        appDao.deleteSubTasksForTask(existing.id)
        appDao.deleteTaskBySyncId(syncId)
    }

    // ---- Notes ----

    private suspend fun reconcileNotes(uid: String): ListenerRegistration = reconcileAndListen(
        uid = uid,
        name = "notes",
        localAll = { appDao.getAllNotesFlow().first() },
        syncIdOf = { it.syncId },
        toMap = { noteToFirestoreMap(it) },
        applyRemote = { applyRemoteNote(it) },
        deleteLocalBySyncId = { appDao.deleteNoteBySyncId(it) }
    )

    suspend fun pushNote(note: NoteEntity) {
        val uid = activeUid ?: return
        collection(uid, "notes").document(note.syncId).set(noteToFirestoreMap(note), SetOptions.merge()).await()
    }

    private suspend fun applyRemoteNote(data: Map<String, Any?>) {
        val syncId = data["syncId"] as? String ?: return
        val remoteUpdatedAt = (data["updatedAt"] as? Long) ?: 0L
        val existing = appDao.getNoteBySyncId(syncId)
        if (existing != null && existing.updatedAt >= remoteUpdatedAt) {
            if (existing.updatedAt > remoteUpdatedAt) pushNote(existing)
            return
        }
        appDao.insertNote(
            NoteEntity(
                id = existing?.id ?: 0,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                date = data["date"] as? String ?: "",
                colorTag = (data["colorTag"] as? Long)?.toInt() ?: 0,
                referenceImageUrls = data["referenceImageUrls"] as? String ?: "",
                tags = data["tags"] as? String ?: "",
                isBold = data["isBold"] as? Boolean ?: false,
                isItalic = data["isItalic"] as? Boolean ?: false,
                fontScale = (data["fontScale"] as? Double)?.toFloat() ?: 1.0f,
                textColorArgb = (data["textColorArgb"] as? Long)?.toInt() ?: 0,
                aiSummary = data["aiSummary"] as? String ?: "",
                syncId = syncId,
                updatedAt = remoteUpdatedAt
            )
        )
    }

    // ---- Captures ----

    private suspend fun reconcileCaptures(uid: String): ListenerRegistration = reconcileAndListen(
        uid = uid,
        name = "captures",
        localAll = { appDao.getAllCapturesFlow().first() },
        syncIdOf = { it.syncId },
        toMap = { captureToFirestoreMap(it) },
        applyRemote = { applyRemoteCapture(it) },
        deleteLocalBySyncId = { appDao.deleteCaptureBySyncId(it) }
    )

    suspend fun pushCapture(capture: CaptureEntity) {
        val uid = activeUid ?: return
        collection(uid, "captures").document(capture.syncId).set(captureToFirestoreMap(capture), SetOptions.merge()).await()
    }

    private suspend fun applyRemoteCapture(data: Map<String, Any?>) {
        val syncId = data["syncId"] as? String ?: return
        val remoteUpdatedAt = (data["updatedAt"] as? Long) ?: 0L
        val existing = appDao.getCaptureBySyncId(syncId)
        if (existing != null && existing.updatedAt >= remoteUpdatedAt) {
            if (existing.updatedAt > remoteUpdatedAt) pushCapture(existing)
            return
        }
        appDao.insertCapture(
            CaptureEntity(
                id = existing?.id ?: 0,
                title = data["title"] as? String ?: "",
                timestamp = data["timestamp"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                category = data["category"] as? String ?: "",
                extractedText = data["extractedText"] as? String ?: "",
                status = data["status"] as? String ?: "New",
                summary = data["summary"] as? String ?: "",
                aiTitle = data["aiTitle"] as? String ?: "",
                detectedLanguage = data["detectedLanguage"] as? String ?: "",
                tags = data["tags"] as? String ?: "",
                entities = data["entities"] as? String ?: "",
                confidence = (data["confidence"] as? Double)?.toFloat() ?: 0f,
                sourceType = data["sourceType"] as? String ?: "screenshot",
                isImportant = data["isImportant"] as? Boolean ?: false,
                syncId = syncId,
                updatedAt = remoteUpdatedAt
            )
        )
    }

    // ---- Events ----

    private suspend fun reconcileEvents(uid: String): ListenerRegistration = reconcileAndListen(
        uid = uid,
        name = "events",
        localAll = { appDao.getAllEventsFlow().first() },
        syncIdOf = { it.syncId },
        toMap = { eventToFirestoreMap(it) },
        applyRemote = { applyRemoteEvent(it) },
        deleteLocalBySyncId = { appDao.deleteEventBySyncId(it) }
    )

    suspend fun pushEvent(event: EventEntity) {
        val uid = activeUid ?: return
        collection(uid, "events").document(event.syncId).set(eventToFirestoreMap(event), SetOptions.merge()).await()
    }

    private suspend fun applyRemoteEvent(data: Map<String, Any?>) {
        val syncId = data["syncId"] as? String ?: return
        val remoteUpdatedAt = (data["updatedAt"] as? Long) ?: 0L
        val existing = appDao.getEventBySyncId(syncId)
        if (existing != null && existing.updatedAt >= remoteUpdatedAt) {
            if (existing.updatedAt > remoteUpdatedAt) pushEvent(existing)
            return
        }
        appDao.insertEvent(
            EventEntity(
                id = existing?.id ?: 0,
                title = data["title"] as? String ?: "",
                timeRange = data["timeRange"] as? String ?: "",
                location = data["location"] as? String ?: "",
                day = (data["day"] as? Long)?.toInt() ?: 1,
                monthName = data["monthName"] as? String ?: "",
                year = (data["year"] as? Long)?.toInt() ?: 2023,
                color = (data["color"] as? Long)?.toInt() ?: 0,
                googleEventId = data["googleEventId"] as? String,
                syncId = syncId,
                updatedAt = remoteUpdatedAt
            )
        )
    }

    // ---- Habits ----

    private suspend fun reconcileHabits(uid: String): ListenerRegistration = reconcileAndListen(
        uid = uid,
        name = "habits",
        localAll = { appDao.getAllHabitsFlow().first() },
        syncIdOf = { it.syncId },
        toMap = { habitToFirestoreMap(it) },
        applyRemote = { applyRemoteHabit(it) },
        deleteLocalBySyncId = { deleteLocalHabitBySyncId(it) }
    )

    suspend fun pushHabit(habit: HabitEntity) {
        val uid = activeUid ?: return
        collection(uid, "habits").document(habit.syncId).set(habitToFirestoreMap(habit), SetOptions.merge()).await()
    }

    private suspend fun applyRemoteHabit(data: Map<String, Any?>) {
        val syncId = data["syncId"] as? String ?: return
        val remoteUpdatedAt = (data["updatedAt"] as? Long) ?: 0L
        val existing = appDao.getHabitBySyncId(syncId)
        if (existing != null && existing.updatedAt >= remoteUpdatedAt) {
            if (existing.updatedAt > remoteUpdatedAt) pushHabit(existing)
            return
        }
        val habit = HabitEntity(
            id = existing?.id ?: 0,
            title = data["title"] as? String ?: "",
            intervalHours = (data["intervalHours"] as? Long)?.toInt() ?: 1,
            isActive = data["isActive"] as? Boolean ?: true,
            createdAt = (data["createdAt"] as? Long) ?: 0L,
            syncId = syncId,
            updatedAt = remoteUpdatedAt,
            currentStreak = (data["currentStreak"] as? Long)?.toInt() ?: 0,
            longestStreak = (data["longestStreak"] as? Long)?.toInt() ?: 0,
            lastCompletedEpochDay = (data["lastCompletedEpochDay"] as? Long) ?: -1
        )
        val localId = if (existing != null) {
            appDao.updateHabit(habit)
            existing.id
        } else {
            appDao.insertHabit(habit).toInt()
        }
        val withLocalId = habit.copy(id = localId)
        if (withLocalId.isActive) HabitScheduler.schedule(appContext, withLocalId) else HabitScheduler.cancel(appContext, localId)
    }

    private suspend fun deleteLocalHabitBySyncId(syncId: String) {
        val existing = appDao.getHabitBySyncId(syncId) ?: return
        HabitScheduler.cancel(appContext, existing.id)
        appDao.deleteHabitBySyncId(syncId)
    }

    // ---- Profile (single doc, no reconcile — AppViewModel seeds it from FirebaseUser) ----

    private fun attachProfileListener(uid: String): ListenerRegistration {
        return userDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists() || snapshot.metadata.hasPendingWrites()) return@addSnapshotListener
            val data = snapshot.data ?: return@addSnapshotListener
            _remoteProfile.value = ProfileSnapshot(
                displayName = data["displayName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String,
                updatedAt = (data["updatedAt"] as? Long) ?: 0L
            )
        }
    }

    suspend fun pushProfile(displayName: String, email: String, photoUrl: String?) {
        val uid = activeUid ?: return
        userDoc(uid).set(
            mapOf(
                "displayName" to displayName,
                "email" to email,
                "photoUrl" to photoUrl,
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    // ---- Deletes & erasure ----

    private suspend fun deleteRemoteDoc(name: String, syncId: String) {
        activeUid?.let { collection(it, name).document(syncId).delete().await() }
    }

    suspend fun deleteRemoteTask(syncId: String) = deleteRemoteDoc("tasks", syncId)
    suspend fun deleteRemoteNote(syncId: String) = deleteRemoteDoc("notes", syncId)
    suspend fun deleteRemoteCapture(syncId: String) = deleteRemoteDoc("captures", syncId)
    suspend fun deleteRemoteEvent(syncId: String) = deleteRemoteDoc("events", syncId)
    suspend fun deleteRemoteHabit(syncId: String) = deleteRemoteDoc("habits", syncId)

    /** Deletes every Firestore doc and Storage file under this user, for the DPDP erase-all-data flow. */
    suspend fun eraseRemoteUserData(uid: String) {
        for (name in listOf("tasks", "notes", "captures", "events", "habits")) {
            val docs = collection(uid, name).get().await()
            for (doc in docs.documents) doc.reference.delete().await()
        }
        userDoc(uid).delete().await()
        try {
            deleteStorageFolderRecursively(storage.reference.child("users/$uid"))
        } catch (_: Exception) {
            // Best-effort: leftover orphaned files aren't user-visible and don't block local erasure.
        }
    }

    private suspend fun deleteStorageFolderRecursively(ref: StorageReference) {
        val listing = ref.listAll().await()
        for (item in listing.items) item.delete().await()
        for (prefix in listing.prefixes) deleteStorageFolderRecursively(prefix)
    }
}
