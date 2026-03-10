package com.newglobe.gemmaocr.inference

object PromptContract {
    fun extractionPrompt(): String = """
        Read the image and return STRICT JSON only.
        Use exactly this schema:
        {"full_text":"string","lines":["string"]}
        Rules:
        - No markdown, no extra keys.
        - Output must be minified JSON on one line.
        - full_text: all text in reading order.
        - lines: one entry per text line.
    """.trimIndent()

    fun repairPrompt(invalidResponse: String): String = """
        Convert the following content into STRICT valid JSON with this exact schema:
        {"full_text":"string","lines":["string"]}
        Return JSON only, minified on one line, and no markdown.
        Content:
        $invalidResponse
    """.trimIndent()
}
