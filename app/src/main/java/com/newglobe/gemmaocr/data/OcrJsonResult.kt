package com.newglobe.gemmaocr.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrJsonResult(
    @SerialName("full_text") val fullText: String,
    val lines: List<String>,
    @SerialName("key_values") val keyValues: List<OcrKeyValue>
)

@Serializable
data class OcrKeyValue(
    val key: String,
    val value: String
)
