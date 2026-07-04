package com.example.data.ai

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Raw OCR result. [confidence] is a 0..1 heuristic over recognized structure. */
data class OcrResult(
    val text: String,
    val confidence: Float,
    val blockCount: Int
)

/**
 * On-device text extraction via ML Kit Text Recognition v2. Free, private, no
 * network round-trip — this is the local-first/privacy pillar for the
 * highest-volume operation in the app.
 */
class OcrService {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extract(context: Context, imageUri: Uri): OcrResult =
        suspendCancellableCoroutine { cont ->
            val image = try {
                InputImage.fromFilePath(context, imageUri)
            } catch (e: Exception) {
                cont.resumeWithException(e)
                return@suspendCancellableCoroutine
            }

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    val blocks = visionText.textBlocks.size
                    // ML Kit doesn't expose a single doc-level confidence; approximate
                    // from how much structured text was recovered so the UI can flag
                    // low-confidence captures for review.
                    val confidence = when {
                        text.isBlank() -> 0f
                        blocks >= 3 && text.length > 40 -> 0.92f
                        blocks >= 1 -> 0.7f
                        else -> 0.4f
                    }
                    cont.resume(OcrResult(text = text.trim(), confidence = confidence, blockCount = blocks))
                }
                .addOnFailureListener { e -> cont.resumeWithException(e) }

            cont.invokeOnCancellation { /* recognizer cleans up its own tasks */ }
        }
}
