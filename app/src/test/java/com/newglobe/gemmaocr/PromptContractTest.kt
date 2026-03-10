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
        assertTrue(prompt.contains("\"full_text\""))
        assertTrue(prompt.contains("\"lines\""))
        assertTrue(prompt.contains("\"key_values\""))
    }
}
