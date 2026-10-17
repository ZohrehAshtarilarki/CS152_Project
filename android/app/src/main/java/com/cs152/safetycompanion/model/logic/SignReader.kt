package com.cs152.safetycompanion.model.logic

import android.graphics.Bitmap

/**
 * Reads and translates sign text ON THE PHONE (no internet) with Google ML Kit.
 * The ML Kit libraries are already added in app/build.gradle.kts.
 *
 * TODO (Traveler side):
 *  - readText: TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
 *      .process(InputImage.fromBitmap(photo, 0)).await().text
 *  - translateToTurkish: Translation.getClient(TranslatorOptions EN -> TR),
 *      downloadModelIfNeeded() during setup (while online), then translate(text).await()
 *  - await() comes from kotlinx-coroutines-play-services (already added).
 */
class SignReader {

    /** Returns the words on the sign, for example "International Transfers". */
    suspend fun readText(photo: Bitmap): String {
        return "" // TODO: ML Kit Text Recognition
    }

    /** English -> Turkish. */
    suspend fun translateToTurkish(text: String): String {
        return text // TODO: ML Kit Translation
    }
}
