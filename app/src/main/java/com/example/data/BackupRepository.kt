package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first

@JsonClass(generateAdapter = false)
data class BackupData(
    val tasks: List<TaskEntity>,
    val subTasks: List<SubTaskEntity>,
    val notes: List<NoteEntity>,
    val events: List<EventEntity>,
    val habits: List<HabitEntity>,
    val captures: List<CaptureEntity>
)

/** Exports/imports all local data as one JSON file, for manual backup and device migration. */
class BackupRepository(private val dao: AppDao) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupData::class.java)

    suspend fun export(): String {
        val data = BackupData(
            tasks = dao.getAllTasksFlow().first(),
            subTasks = dao.getAllSubTasks(),
            notes = dao.getAllNotesFlow().first(),
            events = dao.getAllEventsFlow().first(),
            habits = dao.getAllHabitsFlow().first(),
            captures = dao.getAllCapturesFlow().first()
        )
        return adapter.toJson(data)
    }

    suspend fun import(json: String) {
        val data = adapter.fromJson(json) ?: return
        dao.clearAllTasks()
        dao.clearAllSubTasks()
        dao.clearAllNotes()
        dao.clearAllEvents()
        dao.clearAllHabits()
        dao.clearAllCaptures()
        data.tasks.forEach { dao.insertTask(it) }
        data.subTasks.forEach { dao.insertSubTask(it) }
        data.notes.forEach { dao.insertNote(it) }
        data.events.forEach { dao.insertEvent(it) }
        data.habits.forEach { dao.insertHabit(it) }
        data.captures.forEach { dao.insertCapture(it) }
    }
}
