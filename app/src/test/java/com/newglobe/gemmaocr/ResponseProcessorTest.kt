package com.newglobe.gemmaocr

import com.newglobe.gemmaocr.data.ExtractionOutcome
import com.newglobe.gemmaocr.inference.ResponseProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseProcessorTest {
    @Test
    fun process_validJson_returnsSuccess() {
        val processor = ResponseProcessor()
        val outcome = processor.process(
            """
            {
              "pupil_name":"ok",
              "short_answers":[]
            }
            """.trimIndent()
        )

        assertTrue(outcome is ExtractionOutcome.Success)
    }

    @Test
    fun process_invalidJson_returnsInvalidOutcomeWithRawResponse() {
        val processor = ResponseProcessor()

        val outcome = processor.process("bad one")

        assertTrue(outcome is ExtractionOutcome.InvalidJson)
        assertEquals("bad one", (outcome as ExtractionOutcome.InvalidJson).rawResponse)
    }
}
