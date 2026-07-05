package com.example.data.calendar

import android.app.Activity
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GoogleCalendarEvent(
    val id: String,
    val summary: String,
    val startDateTime: String?,
    val startDate: String?,
    val endDateTime: String?,
    val endDate: String?,
    val location: String
)

private val CALENDAR_READONLY_SCOPE = Scope("https://www.googleapis.com/auth/calendar.readonly")

/**
 * Manual, one-way Google Calendar import: uses the Identity Authorization API (not the
 * heavier google-api-client) to obtain a calendar.readonly access token, then calls the
 * Calendar REST API directly over HTTPS.
 */
class GoogleCalendarService {

    /**
     * Requests calendar.readonly authorization. If consent is already granted (or was granted
     * in a prior call), the returned [AuthorizationResult.accessToken] is non-null. Otherwise
     * [AuthorizationResult.hasResolution] is true and the caller must launch
     * [AuthorizationResult.pendingIntent]'s IntentSender, then call [authorize] again.
     */
    suspend fun authorize(activity: Activity): AuthorizationResult {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(CALENDAR_READONLY_SCOPE))
            .build()
        return Identity.getAuthorizationClient(activity).authorize(request).await()
    }

    /**
     * Fetches upcoming events across every calendar in the user's calendar list — primary plus
     * any shared/subscribed calendars (e.g. regional holiday calendars), not just "primary".
     * Throws on an authorization/API failure so callers can surface the real error instead of
     * silently reporting "0 events imported".
     */
    suspend fun fetchUpcomingEvents(accessToken: String, maxResults: Int = 50): List<GoogleCalendarEvent> {
        val timeMin = java.time.Instant.now().toString()
        return fetchCalendarIds(accessToken).flatMap { calendarId ->
            fetchEventsForCalendar(accessToken, calendarId, timeMin, maxResults)
        }
    }

    private fun getJson(url: URL, accessToken: String): JSONObject {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val errorBody = connection.errorStream?.let { stream ->
                    BufferedReader(InputStreamReader(stream)).use { it.readText() }
                }
                throw IOException("Google Calendar API returned ${connection.responseCode}: $errorBody")
            }
            val body = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchCalendarIds(accessToken: String): List<String> {
        val url = URL("https://www.googleapis.com/calendar/v3/users/me/calendarList?minAccessRole=freeBusyReader")
        val items = getJson(url, accessToken).optJSONArray("items") ?: JSONArray()
        return (0 until items.length()).mapNotNull { i ->
            items.optJSONObject(i)?.optString("id")?.takeIf { it.isNotBlank() }
        }
    }

    private fun fetchEventsForCalendar(
        accessToken: String,
        calendarId: String,
        timeMin: String,
        maxResults: Int
    ): List<GoogleCalendarEvent> {
        val encodedId = URLEncoder.encode(calendarId, "UTF-8")
        val url = URL(
            "https://www.googleapis.com/calendar/v3/calendars/$encodedId/events" +
                "?timeMin=$timeMin&singleEvents=true&orderBy=startTime&maxResults=$maxResults"
        )
        return try {
            val items = getJson(url, accessToken).optJSONArray("items") ?: JSONArray()
            (0 until items.length()).mapNotNull { i ->
                val item = items.optJSONObject(i) ?: return@mapNotNull null
                val id = item.optString("id").ifBlank { return@mapNotNull null }
                val start = item.optJSONObject("start")
                val end = item.optJSONObject("end")
                GoogleCalendarEvent(
                    id = id,
                    summary = item.optString("summary").ifBlank { "Untitled event" },
                    startDateTime = start?.optString("dateTime")?.takeIf { it.isNotBlank() },
                    startDate = start?.optString("date")?.takeIf { it.isNotBlank() },
                    endDateTime = end?.optString("dateTime")?.takeIf { it.isNotBlank() },
                    endDate = end?.optString("date")?.takeIf { it.isNotBlank() },
                    location = item.optString("location")
                )
            }
        } catch (e: Exception) {
            emptyList() // ponytail: one inaccessible calendar shouldn't fail the whole import
        }
    }
}
