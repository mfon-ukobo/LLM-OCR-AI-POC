package com.newglobe.gemmaocr.inference

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.genai.llminference.GraphOptions
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.newglobe.gemmaocr.BuildConfig
import com.newglobe.gemmaocr.data.ExtractionOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GemmaOcrEngine(
    context: Context,
    modelPath: String,
    private val responseProcessor: ResponseProcessor = ResponseProcessor()
) : OcrExtractor, AutoCloseable {
    companion object {
        private const val MAX_REPAIR_INPUT_CHARS = 4000
    }

    private val appContext = context.applicationContext
    private val llmInference: LlmInference = LlmInference.createFromOptions(
        appContext,
        LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxNumImages(1)
            .setMaxTokens(BuildConfig.MODEL_MAX_TOKENS)
            .build()
    )

    override suspend fun extract(image: Bitmap): ExtractionOutcome = withContext(Dispatchers.Default) {
        try {
            val response = runVisionPrompt(image, PromptContract.extractionPrompt())
            responseProcessor.process(response)
        } catch (throwable: Throwable) {
            ExtractionOutcome.RuntimeError(
                message = throwable.message ?: "Gemma inference failed.",
                throwable = throwable
            )
        }
    }

    override suspend fun repairJson(invalidResponse: String): ExtractionOutcome = withContext(Dispatchers.Default) {
        try {
            val repaired = llmInference.generateResponse(
                PromptContract.repairPrompt(truncateRepairInput(invalidResponse))
            )
            responseProcessor.process(repaired)
        } catch (throwable: Throwable) {
            ExtractionOutcome.RuntimeError(
                message = throwable.message ?: "Gemma inference failed.",
                throwable = throwable
            )
        }
    }

    private fun runVisionPrompt(bitmap: Bitmap, prompt: String): String {
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
            session.addImage(BitmapImageBuilder(bitmap).build())
            session.generateResponse()
        } finally {
            session.close()
        }
    }

    override fun close() {
        llmInference.close()
    }

    private fun truncateRepairInput(invalidResponse: String): String {
        if (invalidResponse.length <= MAX_REPAIR_INPUT_CHARS) {
            return invalidResponse
        }
        return invalidResponse.take(MAX_REPAIR_INPUT_CHARS) + "\n...[truncated]"
    }
}
