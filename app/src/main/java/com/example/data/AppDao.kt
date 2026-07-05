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

    @Query("SELECT * FROM tasks WHERE syncId = :syncId")
    suspend fun getTaskBySyncId(syncId: String): TaskEntity?

    @Query("DELETE FROM tasks WHERE syncId = :syncId")
    suspend fun deleteTaskBySyncId(syncId: String)

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

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

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

    @Query("SELECT * FROM sub_tasks WHERE syncId = :syncId")
    suspend fun getSubTaskBySyncId(syncId: String): SubTaskEntity?

    @Query("SELECT * FROM sub_tasks")
    suspend fun getAllSubTasks(): List<SubTaskEntity>

    // Notes
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE syncId = :syncId")
    suspend fun getNoteBySyncId(syncId: String): NoteEntity?

    @Query("DELETE FROM notes WHERE syncId = :syncId")
    suspend fun deleteNoteBySyncId(syncId: String)

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

    @Query("SELECT * FROM captures WHERE syncId = :syncId")
    suspend fun getCaptureBySyncId(syncId: String): CaptureEntity?

    @Query("DELETE FROM captures WHERE syncId = :syncId")
    suspend fun deleteCaptureBySyncId(syncId: String)

    // Events
    @Query("SELECT * FROM events ORDER BY day ASC")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE monthName = :monthName AND year = :year ORDER BY day ASC")
    fun getEventsForMonthFlow(monthName: String, year: Int): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE syncId = :syncId")
    suspend fun getEventBySyncId(syncId: String): EventEntity?

    @Query("SELECT * FROM events WHERE googleEventId = :googleEventId")
    suspend fun getEventByGoogleEventId(googleEventId: String): EventEntity?

    @Query("DELETE FROM events WHERE syncId = :syncId")
    suspend fun deleteEventBySyncId(syncId: String)

    @Query("UPDATE events SET isDone = :done, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markEventDone(id: Int, done: Boolean, updatedAt: Long = System.currentTimeMillis())

    // Habits
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1")
    suspend fun getActiveHabits(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE syncId = :syncId")
    suspend fun getHabitBySyncId(syncId: String): HabitEntity?

    @Query("DELETE FROM habits WHERE syncId = :syncId")
    suspend fun deleteHabitBySyncId(syncId: String)

    // Habit Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY loggedAt DESC")
    fun getLogsForHabitFlow(habitId: Int): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY loggedAt DESC")
    suspend fun getLogsForHabit(habitId: Int): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE epochDay = :epochDay ORDER BY loggedAt ASC")
    suspend fun getLogsForDay(epochDay: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND epochDay >= :fromEpochDay ORDER BY epochDay ASC")
    suspend fun getLogsForHabitSince(habitId: Int, fromEpochDay: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE epochDay >= :fromEpochDay ORDER BY epochDay ASC")
    fun getLogsFromFlow(fromEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Int)

    @Query("DELETE FROM habit_logs")
    suspend fun clearAllHabitLogs()
}
