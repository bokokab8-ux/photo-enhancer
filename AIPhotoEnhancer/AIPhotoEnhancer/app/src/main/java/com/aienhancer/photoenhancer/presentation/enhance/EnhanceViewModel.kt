package com.aienhancer.photoenhancer.presentation.enhance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.TaskStatus
import com.aienhancer.photoenhancer.domain.usecase.EnhanceImageUseCase
import com.aienhancer.photoenhancer.domain.usecase.ExportEnhancedImageUseCase
import com.aienhancer.photoenhancer.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

/**
 * ViewModel for [EnhanceScreen].
 *
 * Reads the source image URI and requested [EnhancementType] from navigation
 * arguments (via [SavedStateHandle], which Hilt's `hiltViewModel()` wires up
 * automatically from the NavHost's backstack entry), then drives the
 * Idle -> Processing -> Success|Error state machine described by [EnhanceUiState]
 * by collecting the [Flow] returned from [EnhanceImageUseCase].
 *
 * Processing is *not* started automatically on screen load - it begins only
 * when [startEnhancement] is invoked (typically from a "Start Enhancing"
 * button while Idle), matching the requirement that the screen show an
 * explicit idle state first.
 */
@HiltViewModel
class EnhanceViewModel @Inject constructor(
    private val enhanceImageUseCase: EnhanceImageUseCase,
    private val exportEnhancedImageUseCase: ExportEnhancedImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sourceImageUri: String = URLDecoder.decode(
        savedStateHandle.get<String>(Screen.Enhance.ARG_IMAGE_URI).orEmpty(),
        "UTF-8"
    )

    private val enhancementType: EnhancementType = EnhancementType.valueOf(
        savedStateHandle.get<String>(Screen.Enhance.ARG_ENHANCEMENT_TYPE)
            ?: EnhancementType.UPSCALE_4X.name
    )

    private val _uiState = MutableStateFlow<EnhanceUiState>(
        EnhanceUiState.Idle(sourceImageUri, enhancementType)
    )
    val uiState: StateFlow<EnhanceUiState> = _uiState.asStateFlow()

    private var enhancementJob: Job? = null

    /** Begins the AI enhancement pipeline; safe to call only once per Idle/Error state. */
    fun startEnhancement() {
        // Avoid starting a duplicate pipeline if one is already running.
        if (_uiState.value is EnhanceUiState.Processing) return

        enhancementJob?.cancel()
        enhancementJob = viewModelScope.launch {
            enhanceImageUseCase(sourceImageUri, enhancementType)
                .onEach { task ->
                    _uiState.value = mapTaskToUiState(task.status)
                }
                .catch { throwable ->
                    _uiState.value = EnhanceUiState.Error(
                        sourceImageUri = sourceImageUri,
                        enhancementType = enhancementType,
                        message = throwable.message ?: "An unexpected error occurred."
                    )
                }
                .collect {}
        }
    }

    private fun mapTaskToUiState(status: TaskStatus): EnhanceUiState = when (status) {
        is TaskStatus.Idle -> EnhanceUiState.Idle(sourceImageUri, enhancementType)

        is TaskStatus.Processing -> EnhanceUiState.Processing(
            sourceImageUri = sourceImageUri,
            enhancementType = enhancementType,
            progress = status.progress,
            stageLabel = status.stage
        )

        is TaskStatus.Success -> EnhanceUiState.Success(
            sourceImageUri = sourceImageUri,
            enhancedImageUri = status.outputUri,
            enhancementType = enhancementType,
            processingDurationMs = status.processingDurationMs
        )

        is TaskStatus.Error -> EnhanceUiState.Error(
            sourceImageUri = sourceImageUri,
            enhancementType = enhancementType,
            message = status.message
        )

        is TaskStatus.Cancelled -> EnhanceUiState.Idle(sourceImageUri, enhancementType)
    }

    /** Retries the pipeline after a failure, from the Error state. */
    fun retry() {
        _uiState.value = EnhanceUiState.Idle(sourceImageUri, enhancementType)
        startEnhancement()
    }

    /** Exports the current Success state's enhanced image to the device gallery. */
    fun exportToGallery() {
        val currentState = _uiState.value
        if (currentState !is EnhanceUiState.Success) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isExporting = true, exportedMessage = null)
            val result = exportEnhancedImageUseCase(currentState.enhancedImageUri)
            val updatedState = _uiState.value
            if (updatedState is EnhanceUiState.Success) {
                _uiState.value = result.fold(
                    onSuccess = {
                        updatedState.copy(
                            isExporting = false,
                            exportedMessage = "Image saved to gallery"
                        )
                    },
                    onFailure = { error ->
                        updatedState.copy(
                            isExporting = false,
                            exportedMessage = "Export failed: ${error.message}"
                        )
                    }
                )
            }
        }
    }

    /** Clears the one-shot export result message after it has been shown to the user. */
    fun consumeExportMessage() {
        val currentState = _uiState.value
        if (currentState is EnhanceUiState.Success) {
            _uiState.value = currentState.copy(exportedMessage = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        enhancementJob?.cancel()
    }
}
