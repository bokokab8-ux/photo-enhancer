package com.aienhancer.photoenhancer.domain.repository

import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.ImageTask
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for running the AI image enhancement pipeline.
 *
 * The data layer provides the concrete implementation (in this project, a
 * simulated pipeline; in production this would call into an on-device model
 * runtime such as TensorFlow Lite / ONNX Runtime, or a remote inference API).
 *
 * Exposing a [Flow] of [ImageTask] lets the presentation layer observe granular
 * progress updates (percentage + stage label) as they happen, rather than just
 * awaiting a single terminal result.
 */
interface ImageEnhancementRepository {

    /**
     * Runs the enhancement pipeline for the given [sourceImageUri] using the given
     * [enhancementType], emitting an updated [ImageTask] every time the underlying
     * status changes (Idle -> Processing(progress=...) -> Success | Error).
     *
     * The returned [Flow] is cold: the pipeline only starts running once a collector
     * subscribes, and cancelling collection (e.g. via viewModelScope cancellation)
     * stops the simulated/real work as soon as possible.
     */
    fun enhanceImage(
        sourceImageUri: String,
        enhancementType: EnhancementType
    ): Flow<ImageTask>

    /**
     * Persists the final enhanced image (referenced by [enhancedImageUri]) into the
     * device's public gallery / MediaStore, returning the resulting content URI.
     */
    suspend fun exportToGallery(enhancedImageUri: String): Result<String>

    /**
     * Returns the historical list of completed/failed tasks, most recent first.
     * Used to populate a "recent enhancements" section if the UI chooses to show one.
     */
    fun observeTaskHistory(): Flow<List<ImageTask>>
}
