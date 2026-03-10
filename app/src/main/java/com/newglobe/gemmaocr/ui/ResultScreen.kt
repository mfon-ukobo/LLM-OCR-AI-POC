package com.newglobe.gemmaocr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.newglobe.gemmaocr.data.ExtractionOutcome

@Composable
fun ResultScreen(
    outcome: ExtractionOutcome,
    onRunAnother: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var showRaw by remember { mutableStateOf(false) }

    val headline = when (outcome) {
        is ExtractionOutcome.Success -> "Extraction Complete"
        is ExtractionOutcome.InvalidJsonAfterRetry -> "Invalid JSON After Retry"
        is ExtractionOutcome.RuntimeError -> "Runtime Error"
    }

    val rendered = when (outcome) {
        is ExtractionOutcome.Success -> {
            if (showRaw) outcome.rawResponse else outcome.normalizedJson
        }

        is ExtractionOutcome.InvalidJsonAfterRetry -> {
            buildString {
                appendLine("First response:")
                appendLine(outcome.firstResponse)
                appendLine()
                appendLine("Retry response:")
                append(outcome.retryResponse)
            }
        }

        is ExtractionOutcome.RuntimeError -> {
            outcome.message
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineSmall
        )

        if (outcome is ExtractionOutcome.Success) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showRaw = !showRaw },
                    modifier = Modifier.testTag("toggle_raw_button")
                ) {
                    Text(if (showRaw) "Show JSON" else "Show Raw")
                }
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(rendered)) },
                    modifier = Modifier.testTag("copy_output_button")
                ) {
                    Text("Copy Output")
                }
            }
        }

        Text(
            text = rendered,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_text")
        )

        Button(
            onClick = onRunAnother,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_another_button")
        ) {
            Text("Run Another Crop")
        }
    }
}
