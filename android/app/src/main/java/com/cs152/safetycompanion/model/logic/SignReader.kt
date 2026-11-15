package com.cs152.safetycompanion.model.logic

import android.graphics.Bitmap
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Reads and translates sign text ON THE PHONE (no internet) with Google ML Kit.
 */
class SignReader {

    /** Returns the words on the sign, for example "International Transfers". */
    suspend fun readText(photo: Bitmap): String {
        return try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(photo, 0)
            val result = recognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            // Keep it simple: return empty string if recognition fails. The rules will handle it.
            ""
        }
    }

    /** English -> Turkish. */
    suspend fun translateToTurkish(text: String): String {
        if (text.isBlank()) return ""
        
        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.TURKISH)
                .build()
            
            val translator = Translation.getClient(options)
            
            // Download model if needed. In a real app this might be done earlier to avoid delay.
            translator.downloadModelIfNeeded().await()
            
            translator.translate(text).await()
        } catch (e: Exception) {
            // Return original text on failure
            text
        }
    }
}
