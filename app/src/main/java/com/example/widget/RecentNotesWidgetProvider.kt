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
import com.example.data.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Shows the 3 most recently updated notes. */
class RecentNotesWidgetProvider : AppWidgetProvider() {

    companion object {
        /** Call after any note mutation so the widget reflects changes immediately. */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, RecentNotesWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, RecentNotesWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }

        private fun buildRemoteViews(context: Context, recentNotes: List<NoteEntity>): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_recent_notes)

            val rowIds = listOf(R.id.widget_note_1, R.id.widget_note_2, R.id.widget_note_3)
            if (recentNotes.isEmpty()) {
                views.setTextViewText(rowIds[0], "No notes yet")
                views.setTextViewText(rowIds[1], "")
                views.setTextViewText(rowIds[2], "")
            } else {
                rowIds.forEachIndexed { index, viewId ->
                    val note = recentNotes.getOrNull(index)
                    views.setTextViewText(viewId, note?.let { "• ${it.title}" } ?: "")
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
                val allNotes: List<NoteEntity> = dao.getAllNotesFlow().first()
                val recentNotes = allNotes.sortedByDescending { it.updatedAt }.take(3)

                val views = buildRemoteViews(context, recentNotes)
                appWidgetIds.forEach { id -> appWidgetManager.updateAppWidget(id, views) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
