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
              "pupil_name": "Ada",
              "short_answers": [
                {"question":"31","answer":"Leaf"}
              ]
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("Ada", parsed?.parsed?.pupilName)
        assertEquals(1, parsed?.parsed?.shortAnswers?.size)
        assertEquals("31", parsed?.parsed?.shortAnswers?.first()?.question)
    }

    @Test
    fun parse_emptyShortAnswers_isValid() {
        val raw = """
            {
              "pupil_name": "",
              "short_answers": []
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("", parsed?.parsed?.pupilName)
        assertEquals(0, parsed?.parsed?.shortAnswers?.size)
    }

    @Test
    fun parse_markdownWrappedJson_extractsPayload() {
        val raw = """
            ```json
            {
              "pupil_name":"Alpha",
              "short_answers":[]
            }
            ```
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("Alpha", parsed?.parsed?.pupilName)
    }

    @Test
    fun parse_missingPupilName_returnsNull() {
        val raw = """{"short_answers":[{"question":"21","answer":"Sun"}]}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_missingShortAnswers_returnsNull() {
        val raw = """{"pupil_name":"Ada"}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }

    @Test
    fun parse_shortAnswersNotArray_returnsNull() {
        val raw = """{"pupil_name":"Ada","short_answers":"bad"}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }
}
