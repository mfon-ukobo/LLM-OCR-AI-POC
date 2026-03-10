package com.newglobe.gemmaocr.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun PickAndCropScreen(
    selectedImage: Bitmap?,
    cropRect: NormalizedRect,
    isExtracting: Boolean,
    transientError: String?,
    onPickImage: () -> Unit,
    onCropChanged: (NormalizedRect) -> Unit,
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
            text = "Crop Image and Extract JSON",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Pick an image, drag a rectangle over the text region, then run extraction.",
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
            CropSelector(
                bitmap = selectedImage,
                initialRect = cropRect,
                onCropChanged = onCropChanged
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

@Composable
private fun CropSelector(
    bitmap: Bitmap,
    initialRect: NormalizedRect,
    onCropChanged: (NormalizedRect) -> Unit
) {
    var canvasSize by remember(bitmap) { mutableStateOf(IntSize.Zero) }
    var dragStart by remember(bitmap) { mutableStateOf<Offset?>(null) }
    var localRect by remember(bitmap) { mutableStateOf(initialRect.normalized()) }

    LaunchedEffect(initialRect, bitmap) {
        localRect = initialRect.normalized()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat())
            .onSizeChanged { canvasSize = it }
            .pointerInput(bitmap) {
                detectDragGestures(
                    onDragStart = { start ->
                        val clamped = start.clamped(canvasSize)
                        dragStart = clamped
                        localRect = fromOffsets(clamped, clamped, canvasSize)
                        onCropChanged(localRect)
                    },
                    onDragEnd = { dragStart = null },
                    onDragCancel = { dragStart = null }
                ) { change, _ ->
                    change.consume()
                    val start = dragStart ?: change.position.clamped(canvasSize)
                    val end = change.position.clamped(canvasSize)
                    localRect = fromOffsets(start, end, canvasSize)
                    onCropChanged(localRect)
                }
            }
            .testTag("crop_surface")
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected image",
            modifier = Modifier.fillMaxSize()
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rectLeft = localRect.left * size.width
            val rectTop = localRect.top * size.height
            val rectRight = localRect.right * size.width
            val rectBottom = localRect.bottom * size.height

            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(0f, 0f),
                size = size.copy(height = rectTop)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(0f, rectBottom),
                size = size.copy(height = size.height - rectBottom)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(0f, rectTop),
                size = androidx.compose.ui.geometry.Size(rectLeft, rectBottom - rectTop)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.25f),
                topLeft = Offset(rectRight, rectTop),
                size = androidx.compose.ui.geometry.Size(size.width - rectRight, rectBottom - rectTop)
            )
            drawRect(
                color = Color(0xFF20C997),
                topLeft = Offset(rectLeft, rectTop),
                size = androidx.compose.ui.geometry.Size(rectRight - rectLeft, rectBottom - rectTop),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }
    }
}

private fun Offset.clamped(bounds: IntSize): Offset {
    if (bounds.width == 0 || bounds.height == 0) return this
    return Offset(
        x = x.coerceIn(0f, bounds.width.toFloat()),
        y = y.coerceIn(0f, bounds.height.toFloat())
    )
}

private fun fromOffsets(start: Offset, end: Offset, canvasSize: IntSize): NormalizedRect {
    if (canvasSize.width == 0 || canvasSize.height == 0) {
        return NormalizedRect.Default
    }
    return NormalizedRect(
        left = minOf(start.x, end.x) / canvasSize.width.toFloat(),
        top = minOf(start.y, end.y) / canvasSize.height.toFloat(),
        right = maxOf(start.x, end.x) / canvasSize.width.toFloat(),
        bottom = maxOf(start.y, end.y) / canvasSize.height.toFloat()
    ).normalized()
}
