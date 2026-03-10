package com.newglobe.gemmaocr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newglobe.gemmaocr.ui.AppUiState
import com.newglobe.gemmaocr.ui.NormalizedRect
import com.newglobe.gemmaocr.ui.PickAndCropScreen
import com.newglobe.gemmaocr.ui.ResultScreen
import com.newglobe.gemmaocr.ui.SetupScreen
import com.newglobe.gemmaocr.ui.SetupUiState

@Composable
fun GemmaOcrApp(
    mainViewModel: MainViewModel = viewModel()
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            mainViewModel.onImageSelected(uri)
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GemmaOcrAppScreen(
                uiState = uiState,
                onRetrySetup = mainViewModel::retrySetup,
                onPickImage = { galleryPicker.launch("image/*") },
                onCropChanged = mainViewModel::updateCropRect,
                onExtract = mainViewModel::runExtraction,
                onClearImage = mainViewModel::clearSelectedImage,
                onDismissError = mainViewModel::dismissTransientError,
                onRestart = mainViewModel::resetResult
            )
        }
    }
}

@Composable
fun GemmaOcrAppScreen(
    uiState: AppUiState,
    onRetrySetup: () -> Unit,
    onPickImage: () -> Unit,
    onCropChanged: (NormalizedRect) -> Unit,
    onExtract: () -> Unit,
    onClearImage: () -> Unit,
    onDismissError: () -> Unit,
    onRestart: () -> Unit
) {
    val setupState = uiState.setupState
    val outcome = uiState.extractionOutcome

    when {
        setupState !is SetupUiState.Ready -> {
            SetupScreen(
                setupState = setupState,
                transientError = uiState.transientError,
                onRetry = onRetrySetup,
                onDismissError = onDismissError
            )
        }

        outcome != null -> {
            ResultScreen(
                outcome = outcome,
                onRunAnother = onRestart
            )
        }

        else -> {
            PickAndCropScreen(
                selectedImage = uiState.selectedImage,
                cropRect = uiState.cropRect,
                isExtracting = uiState.isExtracting,
                transientError = uiState.transientError,
                onPickImage = onPickImage,
                onCropChanged = onCropChanged,
                onExtract = onExtract,
                onClearImage = onClearImage,
                onDismissError = onDismissError
            )
        }
    }
}
