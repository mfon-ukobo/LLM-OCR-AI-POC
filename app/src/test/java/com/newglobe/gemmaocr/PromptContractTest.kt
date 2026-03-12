package com.newglobe.gemmaocr

import com.newglobe.gemmaocr.inference.PromptContract
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptContractTest {
    @Test
    fun extractionPrompt_enforcesJsonSchemaInstructions() {
        val prompt = PromptContract.extractionPrompt()

        assertTrue(prompt.contains("STRICT JSON only"))
        assertTrue(prompt.contains("Read the image"))
        assertTrue(!prompt.contains("image crop"))
        assertTrue(prompt.contains("\"pupil_name\""))
        assertTrue(prompt.contains("\"short_answers\""))
        assertTrue(prompt.contains("\"question\""))
        assertTrue(prompt.contains("\"answer\""))
        assertTrue(!prompt.contains("\"21\""))
        assertTrue(!prompt.contains("\"25\""))
    }
}
