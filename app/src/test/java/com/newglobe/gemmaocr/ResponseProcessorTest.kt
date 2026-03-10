package com.newglobe.gemmaocr

import com.newglobe.gemmaocr.data.ExtractionOutcome
import com.newglobe.gemmaocr.inference.ResponseProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseProcessorTest {
    @Test
    fun process_invalidFirstResponse_retriesExactlyOnce() {
        val processor = ResponseProcessor()
        var retries = 0

        val outcome = processor.process(
            initialResponse = "not json"
        ) {
            retries += 1
            """
            {
              "full_text":"ok",
              "lines":["ok"],
              "key_values":[]
            }
            """.trimIndent()
        }

        assertEquals(1, retries)
        assertTrue(outcome is ExtractionOutcome.Success)
    }

    @Test
    fun process_invalidAfterRetry_returnsInvalidOutcome() {
        val processor = ResponseProcessor()

        val outcome = processor.process(
            initialResponse = "bad one"
        ) { "still bad" }

        assertTrue(outcome is ExtractionOutcome.InvalidJsonAfterRetry)
    }
}
