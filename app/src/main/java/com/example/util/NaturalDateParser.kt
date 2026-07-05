package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate

/** Parses simple natural-language due-date phrases into a [LocalDate]. No NLP library — regex + java.time. */
object NaturalDateParser {

    private val weekdays = mapOf(
        "monday" to DayOfWeek.MONDAY,
        "tuesday" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY,
        "thursday" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY,
        "saturday" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY
    )

    private val inDaysRegex = Regex("""in\s+(\d+)\s+days?""")
    private val nextWeekdayRegex = Regex("""next\s+(\w+)""")

    fun parse(text: String): LocalDate? {
        val today = LocalDate.now()
        val normalized = text.trim().lowercase()
        if (normalized.isEmpty()) return null

        if (normalized == "today") return today
        if (normalized == "tomorrow") return today.plusDays(1)

        inDaysRegex.find(normalized)?.let { match ->
            val days = match.groupValues[1].toIntOrNull() ?: return@let
            return today.plusDays(days.toLong())
        }

        nextWeekdayRegex.find(normalized)?.let { match ->
            val target = weekdays[match.groupValues[1]] ?: return@let
            var candidate = today.plusDays(1)
            while (candidate.dayOfWeek != target) candidate = candidate.plusDays(1)
            return candidate
        }

        weekdays[normalized]?.let { target ->
            var candidate = today.plusDays(1)
            while (candidate.dayOfWeek != target) candidate = candidate.plusDays(1)
            return candidate
        }

        return null
    }
}
