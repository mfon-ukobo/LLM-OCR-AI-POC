package com.newglobe.gemmaocr.ui

sealed interface SetupUiState {
    data object Checking : SetupUiState

    data class Downloading(
        val downloadedBytes: Long,
        val totalBytes: Long?
    ) : SetupUiState

    data class Error(
        val message: String
    ) : SetupUiState

    data object Ready : SetupUiState
}
