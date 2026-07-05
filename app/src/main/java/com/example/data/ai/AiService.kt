package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import org.json.JSONObject

/**
 * Structured result of the AI enrichment pass. Every field here is a
 * *suggestion* — the UI keeps them all editable per the PRD's "AI suggests,
 * user decides" principle.
 */
data class AiAnalysis(
    val title: String,
    val summary: String,
    val category: String,
    val tags: List<String>,
    val entitiesJson: String,
    val language: String,
    val confidence: Float,
    val needsReview: Boolean
)

/**
 * Cloud enrichment via Firebase AI Logic (Gemini Developer API backend).
 *
 * Privacy: only the OCR **text** is ever sent — never the image — mirroring the
 * text-only cloud pattern the PRD commits to. If Firebase isn't configured yet
 * (no google-services.json) or the call fails, [analyze] returns a safe
 * "needs review" fallback built from the raw text so the app never breaks.
 */
class AiService {

    companion object {
        private const val TAG = "AiService"

        val CATEGORIES = listOf(
            "Documents", "Timetable / schedule", "Receipts / bills",
            "Tickets / reservations", "Shopping / product", "Chats / social",
            "Articles / web info", "Notes / ideas", "Code / technical",
            "Media / entertainment", "Places / travel", "Other"
        )
    }

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            }
        )
    }

    /** Plain-text model (no forced JSON mime type) for simple prose responses like note summaries. */
    private val textModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                temperature = 0.3f
            }
        )
    }

    /** Summarizes a user-authored note into 1-2 plain-text sentences. Falls back to a truncated echo of the note on any failure. */
    suspend fun summarizeNote(text: String): String {
        if (text.isBlank()) return ""
        val prompt = """
            Summarize the following personal note in 1-2 concise sentences. Return ONLY the
            summary text, no preamble, no markdown, no quotes.

            NOTE:
            ""${'"'}
            ${text.take(4000)}
            ""${'"'}
        """.trimIndent()
        return try {
            textModel.generateContent(prompt).text?.trim()?.ifBlank { null } ?: text.take(140)
        } catch (e: Exception) {
            Log.w(TAG, "AI summarizeNote failed, using fallback: ${e.message}")
            text.take(140)
        }
    }

    suspend fun analyze(ocrText: String): AiAnalysis {
        if (ocrText.isBlank()) return fallback("")

        val prompt = buildPrompt(ocrText)
        return try {
            val raw = model.generateContent(prompt).text ?: return fallback(ocrText)
            parse(raw, ocrText)
        } catch (e: Exception) {
            // Missing Firebase config, no network, quota, etc. -> graceful degrade.
            Log.w(TAG, "AI analyze failed, using fallback: ${e.message}")
            fallback(ocrText)
        }
    }

    /**
     * Fully on-device, no-network analysis for users who've turned off cloud AI in
     * AI & Privacy settings. Uses lightweight keyword/regex heuristics instead of a
     * generative model — coarser than [analyze] but never leaves the device.
     */
    fun localOnlyAnalysis(ocrText: String): AiAnalysis {
        if (ocrText.isBlank()) return fallback("")

        val lower = ocrText.lowercase()
        val category = when {
            listOf("total", "invoice", "receipt", "amount due", "gst", "paid").any { it in lower } -> "Receipts / bills"
            listOf("boarding", "pnr", "reservation", "ticket", "seat no", "flight").any { it in lower } -> "Tickets / reservations"
            listOf("mon", "tue", "wed", "thu", "fri", "class", "schedule", "timetable", "period").any { it in lower } -> "Timetable / schedule"
            listOf("add to cart", "price", "buy now", "product", "₹", "$").any { it in lower } -> "Shopping / product"
            listOf("http://", "https://", "www.").any { it in lower } -> "Articles / web info"
            listOf("fun ", "class ", "import ", "public ", "def ", "val ", "var ").any { it in lower } -> "Code / technical"
            listOf("chat", "message", "typing", "online", "sent", "delivered").any { it in lower } -> "Chats / social"
            else -> "Other"
        }
        val urls = Regex("https?://\\S+").findAll(ocrText).map { it.value }.toList()
        val prices = Regex("[₹$]\\s?\\d+[\\d,]*(\\.\\d{1,2})?").findAll(ocrText).map { it.value }.toList()
        val dates = Regex("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b").findAll(ocrText).map { it.value }.toList()
        val entities = JSONObject().apply {
            put("urls", org.json.JSONArray(urls))
            put("prices", org.json.JSONArray(prices))
            put("dates", org.json.JSONArray(dates))
        }
        val tags = lower.split(Regex("\\W+"))
            .filter { it.length > 4 }
            .distinct()
            .take(4)

        return AiAnalysis(
            title = firstLine(ocrText).ifBlank { "Untitled capture" },
            summary = "",
            category = category,
            tags = tags,
            entitiesJson = entities.toString(),
            language = "en",
            confidence = 0.55f,
            needsReview = true
        )
    }

    private fun buildPrompt(ocrText: String): String = """
        You are an assistant inside a productivity app that organizes screenshots.
        Analyze the following text that was OCR-extracted from a screenshot and return
        ONLY a JSON object (no markdown) with exactly these keys:
        {
          "title": "a concise <= 8 word heading",
          "summary": "1-2 sentence summary; if the text is too sparse to summarize, return an empty string",
          "category": "one of ${CATEGORIES.joinToString(", ") { "\"$it\"" }}",
          "tags": ["3-5 short lowercase keyword tags"],
          "entities": { "dates": [], "times": [], "urls": [], "phones": [], "prices": [], "codes": [], "addresses": [] },
          "language": "BCP-47 code of the dominant language, e.g. en, hi, kn",
          "confidence": 0.0
        }
        Rules:
        - "category" MUST be exactly one of the allowed values.
        - Only include entity values that actually appear in the text; leave arrays empty otherwise.
        - "confidence" is your 0..1 confidence that the title+category are correct.

        TEXT:
        ""${'"'}
        ${ocrText.take(4000)}
        ""${'"'}
    """.trimIndent()

    private fun parse(raw: String, ocrText: String): AiAnalysis {
        val json = JSONObject(raw.substringAfter('{', "").let { "{$it" }.ifBlank { raw })
        val title = json.optString("title").ifBlank { firstLine(ocrText) }
        val summary = json.optString("summary")
        val category = json.optString("category").takeIf { it in CATEGORIES } ?: "Other"
        val tags = json.optJSONArray("tags")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
        } ?: emptyList()
        val entities = json.optJSONObject("entities")?.toString() ?: "{}"
        val language = json.optString("language").ifBlank { "en" }
        val confidence = json.optDouble("confidence", 0.6).toFloat().coerceIn(0f, 1f)

        return AiAnalysis(
            title = title,
            summary = summary,
            category = category,
            tags = tags,
            entitiesJson = entities,
            language = language,
            confidence = confidence,
            needsReview = confidence < 0.5f || summary.isBlank()
        )
    }

    private fun fallback(ocrText: String): AiAnalysis = AiAnalysis(
        title = firstLine(ocrText).ifBlank { "Untitled capture" },
        summary = "",
        category = "Other",
        tags = emptyList(),
        entitiesJson = "{}",
        language = "en",
        confidence = 0f,
        needsReview = true
    )

    private fun firstLine(text: String): String =
        text.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(60).orEmpty()
}
