package com.newglobe.gemmaocr

import com.newglobe.gemmaocr.inference.JsonValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class JsonValidatorTest {
    @Test
    fun parse_validJson_returnsParsedPayload() {
        val raw = """
            {
              "short_answers": [
                {"question":"31","answer":"Leaf"}
              ]
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals(1, parsed?.parsed?.shortAnswers?.size)
        assertEquals("31", parsed?.parsed?.shortAnswers?.first()?.question)
    }

    @Test
    fun parse_emptyShortAnswers_isValid() {
        val raw = """
            {
              "short_answers": []
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals(0, parsed?.parsed?.shortAnswers?.size)
    }

    @Test
    fun parse_markdownWrappedJson_extractsPayload() {
        val raw = """
            ```json
            {
              "short_answers":[{"question":"A1","answer":"Alpha"}]
            }
            ```
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("A1", parsed?.parsed?.shortAnswers?.first()?.question)
    }

    @Test
    fun parse_missingShortAnswers_returnsNull() {
        val raw = """{"unexpected":"value"}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_shortAnswersNotArray_returnsNull() {
        val raw = """{"short_answers":"bad"}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_itemMissingQuestion_returnsNull() {
        val raw = """{"short_answers":[{"answer":"Sun"}]}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_itemWrongType_returnsNull() {
        val raw = """{"short_answers":[{"question":21,"answer":"Sun"}]}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_normalizesUnknownQuestionAndSortsByQuestionNumber() {
        val raw = """
            {
              "short_answers": [
                {"question":"UNKNOWN","answer":"u"},
                {"question":"10","answer":"ten"},
                {"question":"","answer":"blank"},
                {"question":"2","answer":"two"},
                {"question":"A1","answer":"alpha"}
              ]
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        val answers = parsed!!.parsed.shortAnswers
        assertEquals("2", answers[0].question)
        assertEquals("10", answers[1].question)
        assertEquals("A1", answers[2].question)
        assertEquals("UNKNOWN", answers[3].question)
        assertEquals("UNKNOWN", answers[4].question)
        assertEquals("blank", answers[3].answer)
        assertEquals("u", answers[4].answer)
    }
}
