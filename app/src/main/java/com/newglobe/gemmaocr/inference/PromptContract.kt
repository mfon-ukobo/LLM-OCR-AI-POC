package com.newglobe.gemmaocr.inference

object PromptContract {
    fun extractionPrompt(): String = """
        Read this cropped answer-sheet section and return STRICT JSON only.
        Use exactly this schema:
        {"short_answers":[{"question":"string","answer":"string"}]}
        Rules:
        - No markdown and no extra text outside JSON.
        - Output must be minified JSON on one line.
        - Extract only handwriting found inside answer rectangles.
        - Ignore printed text, bubble choices, and non-handwritten marks.
        - short_answers: array of handwritten answers; can be [] if none.
        - question: detected question label/number for the same rectangle.
        - answer: extracted handwritten answer text.
        - If answer text exists but question number is unreadable, set question to "UNKNOWN".
    """.trimIndent()

    fun repairPrompt(invalidResponse: String): String = """
        Convert the following content into STRICT valid JSON with this exact schema:
        {"short_answers":[{"question":"string","answer":"string"}]}
        Return JSON only, minified on one line, and no markdown.
        Keep only handwriting from answer rectangles.
        Ignore printed text, bubble choices, and non-handwritten marks.
        Use question="UNKNOWN" when answer exists but question number is unreadable.
        short_answers may be [] when no handwritten answers are detected.
        Content:
        $invalidResponse
    """.trimIndent()
}
