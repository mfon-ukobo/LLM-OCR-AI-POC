package com.newglobe.gemmaocr.inference

import android.graphics.Bitmap
import com.newglobe.gemmaocr.data.ExtractionOutcome

interface OcrExtractor {
    suspend fun extract(image: Bitmap): ExtractionOutcome
    suspend fun repairJson(invalidResponse: String): ExtractionOutcome
}
