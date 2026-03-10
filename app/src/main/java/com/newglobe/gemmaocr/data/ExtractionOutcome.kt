package com.newglobe.gemmaocr.data

sealed interface ExtractionOutcome {
    data class Success(
        val result: OcrJsonResult,
        val rawResponse: String,
        val normalizedJson: String
    ) : ExtractionOutcome

    data class InvalidJson(
        val rawResponse: String
    ) : ExtractionOutcome

    data class RuntimeError(
        val message: String,
        val throwable: Throwable? = null
    ) : ExtractionOutcome
}
