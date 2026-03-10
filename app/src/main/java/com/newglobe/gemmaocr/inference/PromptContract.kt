package com.newglobe.gemmaocr.inference

object PromptContract {
    fun extractionPrompt(): String = """
        You are an OCR extraction engine.
        Read the provided image crop and return STRICT JSON only.
        Do not include markdown, explanations, or extra keys.
        Use exactly this schema:
        {
          "full_text": "string",
          "lines": ["string"],
          "key_values": [{"key":"string","value":"string"}]
        }
        Rules:
        - full_text: all recognized text in natural reading order
        - lines: line-by-line text entries
        - key_values: inferred key-value pairs only when confidence is reasonable, otherwise []
        - Always return valid JSON object.
    """.trimIndent()

    fun repairPrompt(invalidResponse: String): String = """
        Convert the following content into STRICT valid JSON with this exact schema:
        {
          "full_text": "string",
          "lines": ["string"],
          "key_values": [{"key":"string","value":"string"}]
        }
        Return JSON only and no markdown.
        Content:
        $invalidResponse
    """.trimIndent()
}
