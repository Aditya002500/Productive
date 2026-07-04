package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM captures")
    suspend fun clearAllCaptures()

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()

    @Query("DELETE FROM sub_tasks")
    suspend fun clearAllSubTasks()

    // Sub-tasks
    @Query("SELECT * FROM sub_tasks WHERE taskId = :taskId ORDER BY id ASC")
    fun getSubTasksForTaskFlow(taskId: Int): Flow<List<SubTaskEntity>>

    @Query("SELECT * FROM sub_tasks WHERE taskId = :taskId ORDER BY id ASC")
    suspend fun getSubTasksForTask(taskId: Int): List<SubTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Delete
    suspend fun deleteSubTask(subTask: SubTaskEntity)

    @Query("DELETE FROM sub_tasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: Int)

    // Notes
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Captures
    @Query("SELECT * FROM captures ORDER BY id DESC")
    fun getAllCapturesFlow(): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun getCaptureById(id: Int): CaptureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapture(capture: CaptureEntity): Long

    @Update
    suspend fun updateCapture(capture: CaptureEntity)

    @Delete
    suspend fun deleteCapture(capture: CaptureEntity)

    // Events
    @Query("SELECT * FROM events ORDER BY day ASC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE monthName = :monthName AND year = :year ORDER BY day ASC")
    fun getEventsForMonthFlow(monthName: String, year: Int): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)
}
