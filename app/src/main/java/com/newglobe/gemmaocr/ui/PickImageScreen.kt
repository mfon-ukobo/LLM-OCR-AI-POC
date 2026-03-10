package com.newglobe.gemmaocr.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PickImageScreen(
    selectedImage: Bitmap?,
    isExtracting: Boolean,
    transientError: String?,
    onPickImage: () -> Unit,
    onExtract: () -> Unit,
    onClearImage: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Process Image and Extract JSON",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Pick an already cropped image and run extraction.",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPickImage,
                enabled = !isExtracting,
                modifier = Modifier.testTag("pick_image_button")
            ) {
                Text("Choose Image")
            }
            OutlinedButton(
                onClick = onClearImage,
                enabled = !isExtracting && selectedImage != null
            ) {
                Text("Clear")
            }
        }

        if (selectedImage == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No image selected yet.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Image(
                bitmap = selectedImage.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(selectedImage.width.toFloat() / selectedImage.height.toFloat()),
                contentScale = ContentScale.Fit
            )
        }

        if (!transientError.isNullOrBlank()) {
            Text(
                text = transientError,
                color = MaterialTheme.colorScheme.error
            )
            TextButton(onClick = onDismissError) {
                Text("Dismiss")
            }
        }

        Button(
            onClick = onExtract,
            enabled = selectedImage != null && !isExtracting,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("extract_button")
        ) {
            if (isExtracting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Extracting...")
            } else {
                Text("Extract JSON")
            }
        }
    }
}
