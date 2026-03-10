package com.newglobe.gemmaocr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newglobe.gemmaocr.data.ExtractionOutcome
import com.newglobe.gemmaocr.data.OcrJsonResult
import com.newglobe.gemmaocr.data.OcrKeyValue
import com.newglobe.gemmaocr.ui.AppUiState
import com.newglobe.gemmaocr.ui.NormalizedRect
import com.newglobe.gemmaocr.ui.SetupUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GemmaOcrFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun setupToPickToResult_flowWithFakeExtractor() {
        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    AppUiState(
                        setupState = SetupUiState.Error("Missing token")
                    )
                )
            }

            val sampleBitmap = remember {
                Bitmap.createBitmap(180, 120, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.WHITE)
                }
            }

            GemmaOcrAppScreen(
                uiState = uiState,
                onRetrySetup = {
                    uiState = uiState.copy(setupState = SetupUiState.Ready)
                },
                onPickImage = {
                    uiState = uiState.copy(selectedImage = sampleBitmap)
                },
                onCropChanged = { rect ->
                    uiState = uiState.copy(cropRect = rect)
                },
                onExtract = {
                    uiState = uiState.copy(
                        extractionOutcome = ExtractionOutcome.Success(
                            result = OcrJsonResult(
                                fullText = "HELLO WORLD",
                                lines = listOf("HELLO WORLD"),
                                keyValues = listOf(OcrKeyValue("sample", "value"))
                            ),
                            rawResponse = """{"full_text":"HELLO WORLD","lines":["HELLO WORLD"],"key_values":[{"key":"sample","value":"value"}]}""",
                            normalizedJson = """
                                {
                                  "full_text": "HELLO WORLD",
                                  "lines": [
                                    "HELLO WORLD"
                                  ],
                                  "key_values": [
                                    {
                                      "key": "sample",
                                      "value": "value"
                                    }
                                  ]
                                }
                            """.trimIndent()
                        ),
                        isExtracting = false
                    )
                },
                onClearImage = {
                    uiState = uiState.copy(selectedImage = null, cropRect = NormalizedRect.Default)
                },
                onDismissError = {
                    uiState = uiState.copy(transientError = null)
                },
                onRestart = {
                    uiState = uiState.copy(extractionOutcome = null)
                }
            )
        }

        composeRule.onNodeWithTag("setup_error").assertTextContains("Missing token")
        composeRule.onNodeWithText("Retry Setup").performClick()
        composeRule.onNodeWithTag("pick_image_button").assertTextContains("Choose Image")
        composeRule.onNodeWithTag("pick_image_button").performClick()
        composeRule.onNodeWithTag("crop_surface").performTouchInput {
            down(center)
            moveBy(androidx.compose.ui.geometry.Offset(20f, 10f))
            up()
        }
        composeRule.onNodeWithTag("extract_button").performClick()
        composeRule.onNodeWithTag("result_text").assertTextContains("HELLO WORLD")
    }

    @Test
    fun setupAndRuntimeErrors_areRendered() {
        composeRule.setContent {
            GemmaOcrAppScreen(
                uiState = AppUiState(
                    setupState = SetupUiState.Error("MODEL_URL is missing."),
                    transientError = "MODEL_URL is missing."
                ),
                onRetrySetup = {},
                onPickImage = {},
                onCropChanged = {},
                onExtract = {},
                onClearImage = {},
                onDismissError = {},
                onRestart = {}
            )
        }

        composeRule.onNodeWithTag("setup_error").assertTextContains("MODEL_URL is missing.")
    }
}
