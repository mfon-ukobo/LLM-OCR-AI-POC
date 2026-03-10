package com.newglobe.gemmaocr.inference

import com.newglobe.gemmaocr.data.ExtractionOutcome

class ResponseProcessor {
    fun process(
        initialResponse: String,
        retrySupplier: (String) -> String
    ): ExtractionOutcome {
        val initialValidated = JsonValidator.parse(initialResponse)
        if (initialValidated != null) {
            return ExtractionOutcome.Success(
                result = initialValidated.parsed,
                rawResponse = initialResponse,
                normalizedJson = initialValidated.normalizedJson
            )
        }

        val retryResponse = retrySupplier(initialResponse)
        val retryValidated = JsonValidator.parse(retryResponse)
        return if (retryValidated != null) {
            ExtractionOutcome.Success(
                result = retryValidated.parsed,
                rawResponse = retryResponse,
                normalizedJson = retryValidated.normalizedJson
            )
        } else {
            ExtractionOutcome.InvalidJsonAfterRetry(
                firstResponse = initialResponse,
                retryResponse = retryResponse
            )
        }
    }
}
