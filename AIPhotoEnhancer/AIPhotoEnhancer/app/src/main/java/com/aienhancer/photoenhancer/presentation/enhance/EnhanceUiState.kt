package com.aienhancer.photoenhancer.presentation.enhance

import com.aienhancer.photoenhancer.domain.model.EnhancementType

/**
 * UI state for [EnhanceScreen], modeled as a sealed hierarchy so the Compose
 * UI can exhaustively `when`-branch on the current phase (Idle / Processing /
 * Success / Error) without relying on nullable fields to imply state, which
 * is the exact pattern requested: explicit Idle, Processing, Success, Error.
 */
sealed class EnhanceUiState {

    /** Nothing has happened yet; shows a "Start Enhancing" call to action. */
    data class Idle(
        val sourceImageUri: String,
        val enhancementType: EnhancementType
    ) : EnhanceUiState()

    /** The AI pipeline is actively running; drives the circular progress indicator. */
    data class Processing(
        val sourceImageUri: String,
        val enhancementType: EnhancementType,
        val progress: Float,
        val stageLabel: String
    ) : EnhanceUiState()

    /** The pipeline finished; drives the Before/After slider and export button. */
    data class Success(
        val sourceImageUri: String,
        val enhancedImageUri: String,
        val enhancementType: EnhancementType,
        val processingDurationMs: Long,
        val isExporting: Boolean = false,
        val exportedMessage: String? = null
    ) : EnhanceUiState()

    /** The pipeline failed; drives an error view with a retry action. */
    data class Error(
        val sourceImageUri: String,
        val enhancementType: EnhancementType,
        val message: String
    ) : EnhanceUiState()
}
