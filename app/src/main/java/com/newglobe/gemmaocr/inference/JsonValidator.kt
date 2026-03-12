package com.newglobe.gemmaocr.inference

import com.newglobe.gemmaocr.data.AnswerSheetJsonResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

object JsonValidator {
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
            Validated(parsed = parsed, normalizedJson = json.encodeToString(AnswerSheetJsonResult.serializer(), parsed))
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
        val pupilName = obj["pupil_name"] as? JsonPrimitive ?: return false
        if (!pupilName.isString) return false
        return obj["short_answers"]?.jsonArray != null
    }
}
