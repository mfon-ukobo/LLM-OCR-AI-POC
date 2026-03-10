package com.newglobe.gemmaocr.model

import com.newglobe.gemmaocr.data.ModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class ModelDownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long?
)

sealed interface ModelProvisionResult {
    data class Ready(
        val modelFile: File,
        val downloadedNow: Boolean
    ) : ModelProvisionResult

    data class ConfigurationError(
        val message: String
    ) : ModelProvisionResult

    data class DownloadError(
        val message: String,
        val throwable: Throwable? = null
    ) : ModelProvisionResult
}

class ModelManager(
    private val httpClient: OkHttpClient = OkHttpClient()
) {
    suspend fun ensureModelReady(
        config: ModelConfig,
        onProgress: (ModelDownloadProgress) -> Unit = {}
    ): ModelProvisionResult = withContext(Dispatchers.IO) {
        if (config.modelUrl.isBlank()) {
            return@withContext ModelProvisionResult.ConfigurationError(
                "MODEL_URL is missing in local.properties."
            )
        }
        if (config.token.isBlank()) {
            return@withContext ModelProvisionResult.ConfigurationError(
                "HF_TOKEN is missing in local.properties."
            )
        }
        if (config.localPath.isBlank()) {
            return@withContext ModelProvisionResult.ConfigurationError(
                "Local model path is empty."
            )
        }

        val targetFile = File(config.localPath)
        if (targetFile.exists() && targetFile.length() > 0L) {
            return@withContext ModelProvisionResult.Ready(
                modelFile = targetFile,
                downloadedNow = false
            )
        }

        val parent = targetFile.parentFile
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return@withContext ModelProvisionResult.DownloadError(
                "Failed to create model directory: ${parent.absolutePath}"
            )
        }

        val request = Request.Builder()
            .url(config.modelUrl)
            .addHeader("Authorization", "Bearer ${config.token}")
            .build()

        return@withContext try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val snippet = response.body?.string()?.take(240).orEmpty()
                    return@use ModelProvisionResult.DownloadError(
                        "Model download failed (${response.code} ${response.message}). $snippet"
                    )
                }

                val body = response.body ?: return@use ModelProvisionResult.DownloadError(
                    "Model download failed: empty response body."
                )
                val contentLength = body.contentLength().takeIf { it >= 0L }
                val tmp = File(targetFile.absolutePath + ".part")

                body.byteStream().use { input ->
                    tmp.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            onProgress(
                                ModelDownloadProgress(
                                    downloadedBytes = downloaded,
                                    totalBytes = contentLength
                                )
                            )
                        }
                    }
                }

                if (tmp.length() == 0L) {
                    return@use ModelProvisionResult.DownloadError(
                        "Downloaded model file is empty."
                    )
                }

                if (targetFile.exists()) {
                    targetFile.delete()
                }
                if (!tmp.renameTo(targetFile)) {
                    tmp.copyTo(targetFile, overwrite = true)
                    tmp.delete()
                }

                ModelProvisionResult.Ready(
                    modelFile = targetFile,
                    downloadedNow = true
                )
            }
        } catch (throwable: Throwable) {
            ModelProvisionResult.DownloadError(
                message = throwable.message ?: "Unknown model download error.",
                throwable = throwable
            )
        }
    }
}
