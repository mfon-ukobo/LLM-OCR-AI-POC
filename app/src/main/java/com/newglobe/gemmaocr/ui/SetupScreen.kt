package com.newglobe.gemmaocr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(
    setupState: SetupUiState,
    transientError: String?,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gemma 3n Model Setup",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "The app prepares a local model before OCR extraction can run.",
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )

        when (setupState) {
            SetupUiState.Checking -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .testTag("setup_checking")
                )
                Text(text = "Checking model cache...", modifier = Modifier.padding(top = 12.dp))
            }

            is SetupUiState.Downloading -> {
                val fraction = setupState.totalBytes?.takeIf { it > 0L }?.let {
                    setupState.downloadedBytes.toFloat() / it.toFloat()
                }
                LinearProgressIndicator(
                    progress = { fraction?.coerceIn(0f, 1f) ?: 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .testTag("setup_downloading")
                )
                Text(
                    text = "Downloading model...",
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = formatBytes(setupState.downloadedBytes, setupState.totalBytes),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            is SetupUiState.Error -> {
                Text(
                    text = setupState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .testTag("setup_error"),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Retry Setup")
                }
            }

            SetupUiState.Ready -> {
                Text(
                    text = "Model ready.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        if (!transientError.isNullOrBlank() && setupState !is SetupUiState.Error) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp))
            Text(
                text = transientError,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onDismissError) {
                Text("Dismiss")
            }
        }
    }
}

private fun formatBytes(downloaded: Long, total: Long?): String {
    fun toMb(value: Long): String = String.format("%.1f MB", value / (1024f * 1024f))
    return if (total != null && total > 0L) {
        "${toMb(downloaded)} / ${toMb(total)}"
    } else {
        toMb(downloaded)
    }
}
