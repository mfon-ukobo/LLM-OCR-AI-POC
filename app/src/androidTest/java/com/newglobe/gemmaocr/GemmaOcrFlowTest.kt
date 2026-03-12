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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.newglobe.gemmaocr.data.AnswerSheetJsonResult
import com.newglobe.gemmaocr.data.ExtractionOutcome
import com.newglobe.gemmaocr.data.ShortAnswerItem
import com.newglobe.gemmaocr.ui.AppUiState
import com.newglobe.gemmaocr.ui.SetupUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GemmaOcrFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun setupToPickToManualRepair_flowWithFakeExtractor() {
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
                onExtract = {
                    uiState = uiState.copy(
                        extractionOutcome = ExtractionOutcome.InvalidJson(
                            rawResponse = "invalid raw output"
                        ),
                        isExtracting = false
                    )
                },
                onRepairJson = {
                    uiState = uiState.copy(
                        extractionOutcome = ExtractionOutcome.Success(
                            result = AnswerSheetJsonResult(
                                pupilName = "HELLO WORLD",
                                shortAnswers = listOf(
                                    ShortAnswerItem(question = "21", answer = "sample")
                                )
                            ),
                            rawResponse = """{"pupil_name":"HELLO WORLD","short_answers":[{"question":"21","answer":"sample"}]}""",
                            normalizedJson = """
                                {
                                  "pupil_name": "HELLO WORLD",
                                  "short_answers": [
                                    {
                                      "question": "21",
                                      "answer": "sample"
                                    }
                                  ]
                                }
                            """.trimIndent()
                        ),
                        isExtracting = false
                    )
                },
                onClearImage = {
                    uiState = uiState.copy(selectedImage = null)
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
        composeRule.onNodeWithTag("extract_button").performClick()
        composeRule.onNodeWithTag("result_text").assertTextContains("invalid raw output")
        composeRule.onNodeWithTag("repair_json_button").assertTextContains("Repair JSON")
        composeRule.onNodeWithTag("repair_json_button").performClick()
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
                onExtract = {},
                onRepairJson = {},
                onClearImage = {},
                onDismissError = {},
                onRestart = {}
            )
        }

        composeRule.onNodeWithTag("setup_error").assertTextContains("MODEL_URL is missing.")
    }
}
