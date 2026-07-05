package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Shows up to 3 incomplete tasks, ranked the same way as Home's Priority Tasks. */
class TodaysTasksWidgetProvider : AppWidgetProvider() {

    companion object {
        private fun priorityRank(priority: String): Int = when (priority) {
            "High" -> 0
            "Medium" -> 1
            "Low" -> 2
            else -> 3
        }

        /** Call after any task mutation so the widget reflects changes immediately. */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TodaysTasksWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, TodaysTasksWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }

        private fun buildRemoteViews(context: Context, topTasks: List<TaskEntity>): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_todays_tasks)

            val rowIds = listOf(R.id.widget_task_1, R.id.widget_task_2, R.id.widget_task_3)
            if (topTasks.isEmpty()) {
                views.setTextViewText(rowIds[0], "All caught up!")
                views.setTextViewText(rowIds[1], "")
                views.setTextViewText(rowIds[2], "")
            } else {
                rowIds.forEachIndexed { index, viewId ->
                    val task = topTasks.getOrNull(index)
                    views.setTextViewText(viewId, task?.let { "• ${it.title}" } ?: "")
                }
            }

            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            return views
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).appDao()
                // One-shot read: widgets update periodically/on-demand, no need to stay subscribed.
                val allTasks: List<TaskEntity> = dao.getAllTasksFlow().first()
                val topTasks = allTasks
                    .filter { !it.isCompleted }
                    .sortedWith(compareBy({ priorityRank(it.priority) }, { it.dueDate }, { it.dueTime }))
                    .take(3)

                val views = buildRemoteViews(context, topTasks)
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
