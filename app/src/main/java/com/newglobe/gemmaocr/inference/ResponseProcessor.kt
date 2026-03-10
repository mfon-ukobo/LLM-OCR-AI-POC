package com.newglobe.gemmaocr.inference

import com.newglobe.gemmaocr.data.ExtractionOutcome

class ResponseProcessor {
    fun process(rawResponse: String): ExtractionOutcome {
        val validated = JsonValidator.parse(rawResponse)
        return if (validated != null) {
            ExtractionOutcome.Success(
                result = validated.parsed,
                rawResponse = rawResponse,
                normalizedJson = validated.normalizedJson
            )
        } else {
            ExtractionOutcome.InvalidJson(rawResponse = rawResponse)
        }
    }
}
