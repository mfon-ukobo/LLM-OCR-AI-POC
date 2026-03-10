package com.newglobe.gemmaocr.ui

import android.graphics.Bitmap
import com.newglobe.gemmaocr.data.ExtractionOutcome

data class AppUiState(
    val setupState: SetupUiState = SetupUiState.Checking,
    val selectedImage: Bitmap? = null,
    val cropRect: NormalizedRect = NormalizedRect.Default,
    val isExtracting: Boolean = false,
    val extractionOutcome: ExtractionOutcome? = null,
    val transientError: String? = null
)
