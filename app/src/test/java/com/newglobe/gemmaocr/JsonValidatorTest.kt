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
              "full_text": "Hello",
              "lines": ["Hello"],
              "key_values": [{"key":"Name","value":"Ada"}]
            }
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("Hello", parsed?.parsed?.fullText)
        assertEquals(1, parsed?.parsed?.lines?.size)
        assertEquals("Name", parsed?.parsed?.keyValues?.first()?.key)
    }

    @Test
    fun parse_markdownWrappedJson_extractsPayload() {
        val raw = """
            ```json
            {
              "full_text":"Alpha",
              "lines":["Alpha"],
              "key_values":[]
            }
            ```
        """.trimIndent()

        val parsed = JsonValidator.parse(raw)

        assertNotNull(parsed)
        assertEquals("Alpha", parsed?.parsed?.fullText)
    }

    @Test
    fun parse_malformedJson_returnsNull() {
        val raw = """{"full_text":"Oops","lines":["A"],"key_values":[}"""

        val parsed = JsonValidator.parse(raw)

        assertNull(parsed)
    }
}
