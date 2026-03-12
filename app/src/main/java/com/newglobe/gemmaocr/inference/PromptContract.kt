package com.newglobe.gemmaocr.inference

object PromptContract {
    fun extractionPrompt(): String = """
        Read the image and return STRICT JSON only.
        Use exactly this schema:
        {"pupil_name":"string","short_answers":[{"question":"string","answer":"string"}]}
        Rules:
        - No markdown and no extra text outside JSON.
        - Output must be minified JSON on one line.
        - pupil_name: extracted pupil name, or "" when unreadable/missing.
        - short_answers: array of handwritten answers; can be [] if none.
        - question: any detected question label/number from the sheet.
        - answer: extracted handwritten answer text.
    """.trimIndent()

    fun repairPrompt(invalidResponse: String): String = """
        Convert the following content into STRICT valid JSON with this exact schema:
        {"pupil_name":"string","short_answers":[{"question":"string","answer":"string"}]}
        Return JSON only, minified on one line, and no markdown.
        short_answers may be [] when no handwritten answers are detected.
        Content:
        $invalidResponse
    """.trimIndent()
}
