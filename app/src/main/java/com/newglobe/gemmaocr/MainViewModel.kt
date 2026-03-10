package com.newglobe.gemmaocr

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.newglobe.gemmaocr.data.ModelConfig
import com.newglobe.gemmaocr.inference.GemmaOcrEngine
import com.newglobe.gemmaocr.inference.OcrExtractor
import com.newglobe.gemmaocr.model.ModelDownloadProgress
import com.newglobe.gemmaocr.model.ModelManager
import com.newglobe.gemmaocr.model.ModelProvisionResult
import com.newglobe.gemmaocr.ui.AppUiState
import com.newglobe.gemmaocr.ui.NormalizedRect
import com.newglobe.gemmaocr.ui.SetupUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

fun interface OcrExtractorFactory {
    fun create(context: Context, modelPath: String): OcrExtractor
}

class MainViewModel(
    application: Application,
    private val modelManager: ModelManager,
    private val extractorFactory: OcrExtractorFactory
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        modelManager = ModelManager(),
        extractorFactory = OcrExtractorFactory { context, modelPath ->
            GemmaOcrEngine(context, modelPath)
        }
    )

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private var extractor: OcrExtractor? = null

    init {
        prepareModel()
    }

    fun retrySetup() {
        prepareModel()
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            decodeBitmapFromUri(context, uri)
                .onSuccess { bitmap ->
                    _uiState.update {
                        it.copy(
                            selectedImage = bitmap,
                            cropRect = NormalizedRect.Default,
                            extractionOutcome = null,
                            transientError = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(transientError = throwable.message ?: "Unable to decode image.")
                    }
                }
        }
    }

    fun updateCropRect(rect: NormalizedRect) {
        _uiState.update { it.copy(cropRect = rect.normalized()) }
    }

    fun clearSelectedImage() {
        _uiState.update {
            it.copy(
                selectedImage = null,
                cropRect = NormalizedRect.Default,
                extractionOutcome = null,
                transientError = null
            )
        }
    }

    fun dismissTransientError() {
        _uiState.update { it.copy(transientError = null) }
    }

    fun resetResult() {
        _uiState.update { it.copy(extractionOutcome = null, transientError = null) }
    }

    fun runExtraction() {
        val state = _uiState.value
        val bitmap = state.selectedImage
        if (bitmap == null) {
            _uiState.update { it.copy(transientError = "Choose an image first.") }
            return
        }
        if (!state.cropRect.isUsable()) {
            _uiState.update { it.copy(transientError = "Select a larger crop area.") }
            return
        }
        val activeExtractor = extractor
        if (activeExtractor == null) {
            _uiState.update { it.copy(transientError = "Model is not ready yet.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExtracting = true, extractionOutcome = null, transientError = null) }
            val cropResult = runCatching { cropBitmap(bitmap, state.cropRect) }
            if (cropResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isExtracting = false,
                        transientError = cropResult.exceptionOrNull()?.message ?: "Crop failed."
                    )
                }
                return@launch
            }

            val cropped = cropResult.getOrThrow()
            val outcome = activeExtractor.extract(cropped)
            _uiState.update {
                it.copy(
                    isExtracting = false,
                    extractionOutcome = outcome
                )
            }
        }
    }

    private fun prepareModel() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    setupState = SetupUiState.Checking,
                    transientError = null
                )
            }

            val config = buildModelConfig()
            val result = modelManager.ensureModelReady(config, ::onDownloadProgress)
            when (result) {
                is ModelProvisionResult.Ready -> {
                    val context = getApplication<Application>()
                    closeExtractor()
                    extractor = extractorFactory.create(context, result.modelFile.absolutePath)
                    _uiState.update {
                        it.copy(
                            setupState = SetupUiState.Ready,
                            transientError = null
                        )
                    }
                }

                is ModelProvisionResult.ConfigurationError -> {
                    _uiState.update {
                        it.copy(
                            setupState = SetupUiState.Error(result.message),
                            transientError = result.message
                        )
                    }
                }

                is ModelProvisionResult.DownloadError -> {
                    _uiState.update {
                        it.copy(
                            setupState = SetupUiState.Error(result.message),
                            transientError = result.message
                        )
                    }
                }
            }
        }
    }

    private fun onDownloadProgress(progress: ModelDownloadProgress) {
        _uiState.update {
            it.copy(
                setupState = SetupUiState.Downloading(
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = progress.totalBytes
                )
            )
        }
    }

    private fun buildModelConfig(): ModelConfig {
        val modelUrl = BuildConfig.MODEL_URL.trim()
        val token = BuildConfig.HF_TOKEN.trim()
        val fileNameFromUrl = modelUrl.substringAfterLast('/').substringBefore('?').ifBlank {
            "gemma3n_e2b.task"
        }
        val localPath = File(
            getApplication<Application>().filesDir,
            "models/$fileNameFromUrl"
        ).absolutePath
        return ModelConfig(
            modelUrl = modelUrl,
            token = token,
            localPath = localPath
        )
    }

    private fun closeExtractor() {
        val closable = extractor as? AutoCloseable ?: return
        runCatching { closable.close() }
    }

    override fun onCleared() {
        closeExtractor()
        super.onCleared()
    }
}
