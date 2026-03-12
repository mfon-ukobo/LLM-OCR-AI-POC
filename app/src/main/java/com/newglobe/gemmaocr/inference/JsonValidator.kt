package com.newglobe.gemmaocr.inference

import com.newglobe.gemmaocr.data.AnswerSheetJsonResult
import com.newglobe.gemmaocr.data.ShortAnswerItem
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

object JsonValidator {
    private const val UNKNOWN_QUESTION = "UNKNOWN"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    data class Validated(
        val parsed: AnswerSheetJsonResult,
        val normalizedJson: String
    )

    fun parse(rawResponse: String): Validated? {
        val jsonPayload = extractJsonObject(rawResponse) ?: return null
        return try {
            val element = json.parseToJsonElement(jsonPayload)
            if (!hasSchemaShape(element)) {
                return null
            }
            val parsed = json.decodeFromJsonElement<AnswerSheetJsonResult>(element)
            val normalized = normalizeParsed(parsed)
            Validated(
                parsed = normalized,
                normalizedJson = json.encodeToString(AnswerSheetJsonResult.serializer(), normalized)
            )
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun extractJsonObject(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed
        }

        val fenced = extractFencedCode(trimmed)
        if (fenced != null && fenced.startsWith("{") && fenced.endsWith("}")) {
            return fenced
        }

        return extractFirstBalancedObject(trimmed)
    }

    private fun extractFencedCode(text: String): String? {
        val start = text.indexOf("```")
        if (start < 0) return null
        val end = text.indexOf("```", start + 3)
        if (end < 0) return null

        val content = text.substring(start + 3, end).trim()
        return if (content.startsWith("json", ignoreCase = true)) {
            content.substringAfter('\n').trim()
        } else {
            content
        }
    }

    private fun extractFirstBalancedObject(text: String): String? {
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        for (index in start until text.length) {
            when (text[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return text.substring(start, index + 1).trim()
                    }
                }
            }
        }
        return null
    }

    private fun hasSchemaShape(element: JsonElement): Boolean {
        val obj = element as? JsonObject ?: return false
        val shortAnswers = obj["short_answers"]?.jsonArray ?: return false
        return shortAnswers.all { item ->
            val itemObject = item as? JsonObject ?: return@all false
            val question = itemObject["question"] as? JsonPrimitive ?: return@all false
            if (!question.isString) return@all false
            val answer = itemObject["answer"] as? JsonPrimitive ?: return@all false
            answer.isString
        }
    }

    private fun normalizeParsed(parsed: AnswerSheetJsonResult): AnswerSheetJsonResult {
        val normalizedAnswers = parsed.shortAnswers
            .map { item ->
                val normalizedQuestion = item.question.trim().ifBlank { UNKNOWN_QUESTION }
                ShortAnswerItem(
                    question = normalizedQuestion,
                    answer = item.answer.trim()
                )
            }
            .sortedWith(shortAnswerComparator())
        return parsed.copy(shortAnswers = normalizedAnswers)
    }

    private fun shortAnswerComparator(): Comparator<ShortAnswerItem> =
        Comparator { left, right ->
            val leftNumber = left.question.toLongOrNull()
            val rightNumber = right.question.toLongOrNull()

            when {
                leftNumber != null && rightNumber != null -> {
                    val byNumber = leftNumber.compareTo(rightNumber)
                    if (byNumber != 0) {
                        byNumber
                    } else {
                        left.answer.compareTo(right.answer, ignoreCase = true)
                    }
                }

                leftNumber != null -> -1
                rightNumber != null -> 1

                else -> {
                    val byQuestion = left.question.compareTo(right.question, ignoreCase = true)
                    if (byQuestion != 0) {
                        byQuestion
                    } else {
                        left.answer.compareTo(right.answer, ignoreCase = true)
                    }
                }
            }
    }
}
