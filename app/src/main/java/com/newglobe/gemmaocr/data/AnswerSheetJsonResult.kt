package com.newglobe.gemmaocr.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnswerSheetJsonResult(
    @SerialName("pupil_name") val pupilName: String,
    @SerialName("short_answers") val shortAnswers: List<ShortAnswerItem>
)

@Serializable
data class ShortAnswerItem(
    val question: String = "",
    val answer: String = ""
)
