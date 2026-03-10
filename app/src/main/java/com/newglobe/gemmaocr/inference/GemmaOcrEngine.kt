package com.newglobe.gemmaocr.inference

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.newglobe.gemmaocr.data.ExtractionOutcome
import com.newglobe.gemmaocr.ensureArgb8888
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GemmaOcrEngine(
    context: Context,
    modelPath: String,
    private val responseProcessor: ResponseProcessor = ResponseProcessor()
) : OcrExtractor, AutoCloseable {

    private val appContext = context.applicationContext
    private val llmInference: LlmInference = LlmInference.createFromOptions(
        appContext,
        LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxNumImages(1)
            .setMaxTokens(1024)
            .build()
    )

    override suspend fun extract(crop: Bitmap): ExtractionOutcome = withContext(Dispatchers.Default) {
        try {
            val initial = runVisionPrompt(crop, PromptContract.extractionPrompt())
            responseProcessor.process(initialResponse = initial) { invalidResponse ->
                llmInference.generateResponse(PromptContract.repairPrompt(invalidResponse))
            }
        } catch (throwable: Throwable) {
            ExtractionOutcome.RuntimeError(
                message = throwable.message ?: "Gemma inference failed.",
                throwable = throwable
            )
        }
    }

    private fun runVisionPrompt(bitmap: Bitmap, prompt: String): String {
        val argbBitmap = ensureArgb8888(bitmap)
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setGraphOptions(
                GraphOptions.builder()
                    .setEnableVisionModality(true)
                    .setEnableAudioModality(false)
                    .build()
            )
            .setTemperature(0.0f)
            .setTopK(1)
            .setTopP(0.1f)
            .build()

        val session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions)
        return try {
            session.addQueryChunk(prompt)
            session.addImage(BitmapImageBuilder(argbBitmap).build())
            session.generateResponse()
        } finally {
            session.close()
        }
    }

    override fun close() {
        llmInference.close()
    }
}
