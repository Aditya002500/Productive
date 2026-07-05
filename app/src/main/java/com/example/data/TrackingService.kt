package com.example.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps a habit's GPS tracking session (and its ongoing notification)
 * alive after the user leaves the app. State lives here, not in AppViewModel, so it survives
 * independently of the Activity/ViewModel lifecycle — AppViewModel just proxies to it.
 */
class TrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "habit_tracking"
        private const val NOTIFICATION_ID = 9002
        private const val ACTION_START = "com.example.action.TRACKING_START"
        private const val ACTION_PAUSE = "com.example.action.TRACKING_PAUSE"
        private const val ACTION_END = "com.example.action.TRACKING_END"

        val trackingHabitId = MutableStateFlow<Int?>(null)
        val trackingIsRunning = MutableStateFlow(false)
        val trackingSeconds = MutableStateFlow(0)
        val trackingDistanceMeters = MutableStateFlow(0f)
        val trackingPath = MutableStateFlow<List<LatLng>>(emptyList())

        /** Resets session counters when switching to a different habit. Pure state, no Service call. */
        fun prepareSession(habitId: Int) {
            if (trackingHabitId.value == habitId) return
            trackingHabitId.value = habitId
            trackingSeconds.value = 0
            trackingDistanceMeters.value = 0f
            trackingPath.value = emptyList()
        }

        fun startOrResume(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, TrackingService::class.java).setAction(ACTION_START))
        }

        /** Only ever called while the service is already running (UI gates the button on it). */
        fun pause(context: Context) {
            context.startService(Intent(context, TrackingService::class.java).setAction(ACTION_PAUSE))
        }

        /** Only ever called while the service is already running (UI gates the button on it). */
        fun end(context: Context) {
            context.startService(Intent(context, TrackingService::class.java).setAction(ACTION_END))
        }
    }

    private var locationTracker: LocationTracker? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseTracking()
            ACTION_END -> {
                stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> startTracking()
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        return START_NOT_STICKY
    }

    private fun startTracking() {
        trackingIsRunning.value = true
        val tracker = locationTracker ?: LocationTracker(applicationContext).also { locationTracker = it }
        tracker.start { distance, path ->
            trackingDistanceMeters.value = distance
            trackingPath.value = path
        }
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                trackingSeconds.value += 1
                if (trackingSeconds.value % 5 == 0) updateNotification()
            }
        }
    }

    private fun pauseTracking() {
        trackingIsRunning.value = false
        locationTracker?.pause()
        timerJob?.cancel()
    }

    private fun stopTracking() {
        trackingIsRunning.value = false
        timerJob?.cancel()
        locationTracker?.stop()
        locationTracker = null
        trackingHabitId.value = null
        trackingSeconds.value = 0
        trackingDistanceMeters.value = 0f
        trackingPath.value = emptyList()
    }

    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(NotificationManager::class.java)
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            notificationManager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Habit Tracking", NotificationManager.IMPORTANCE_LOW)
            )
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = trackingSeconds.value / 60
        val seconds = trackingSeconds.value % 60
        val km = trackingDistanceMeters.value / 1000f
        val status = if (trackingIsRunning.value) "Tracking" else "Paused"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("$status habit — %02d:%02d".format(minutes, seconds))
            .setContentText("%.2f km so far. Tap to return to CaptureFlow.".format(km))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .build()
    }

    private fun updateNotification() {
        if (trackingHabitId.value == null) return
        try {
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
        } catch (_: SecurityException) {
            // Notification permission not granted — tracking still runs, just not visible.
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }
}
