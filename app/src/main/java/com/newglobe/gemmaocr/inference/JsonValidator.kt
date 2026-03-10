package com.newglobe.gemmaocr.inference

import com.newglobe.gemmaocr.data.OcrJsonResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object JsonValidator {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        isLenient = true
    }

    data class Validated(
        val parsed: OcrJsonResult,
        val normalizedJson: String
    )

    fun parse(rawResponse: String): Validated? {
        val jsonPayload = extractJsonObject(rawResponse) ?: return null
        return try {
            val element = json.parseToJsonElement(jsonPayload)
            if (!hasSchemaShape(element)) {
                return null
            }
            val parsed = json.decodeFromJsonElement<OcrJsonResult>(element)
            Validated(parsed = parsed, normalizedJson = json.encodeToString(OcrJsonResult.serializer(), parsed))
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
        val fullText = obj["full_text"] as? JsonPrimitive ?: return false
        if (!fullText.isString) return false

        val lines = obj["lines"]?.jsonArray ?: return false
        if (lines.any { (it as? JsonPrimitive)?.isString != true }) return false

        val keyValues = obj["key_values"]?.jsonArray ?: return false
        val keyValueShapeOk = keyValues.all { item ->
            val kvObject = item as? JsonObject ?: return@all false
            val key = kvObject["key"]?.jsonPrimitive
            val value = kvObject["value"]?.jsonPrimitive
            key != null && key.isString && value != null && value.isString
        }
        return keyValueShapeOk
    }
}
