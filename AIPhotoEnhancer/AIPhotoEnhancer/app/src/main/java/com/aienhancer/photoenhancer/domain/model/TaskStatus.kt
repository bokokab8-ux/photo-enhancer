package com.aienhancer.photoenhancer.domain.model

/**
 * Represents the lifecycle state of an [ImageTask] as it moves through the
 * AI enhancement pipeline. Modeled as a sealed class (rather than a plain enum)
 * so that states which carry payload data (Processing progress, Error message)
 * can do so type-safely without nullable side-fields on the entity itself.
 */
sealed class TaskStatus {

    /** The task has been created but enhancement has not started yet. */
    data object Idle : TaskStatus()

    /**
     * The task is actively being processed by the AI pipeline.
     *
     * @param progress a value between 0.0f and 1.0f representing overall completion.
     * @param stage a human-readable description of the current pipeline stage,
     *  e.g. "Analyzing image", "Upscaling", "Denoising", "Finalizing".
     */
    data class Processing(
        val progress: Float,
        val stage: String
    ) : TaskStatus()

    /**
     * The task finished successfully and an enhanced image is available.
     *
     * @param outputUri the content/file URI of the enhanced output image.
     * @param processingDurationMs total wall-clock time the pipeline took, in milliseconds.
     */
    data class Success(
        val outputUri: String,
        val processingDurationMs: Long
    ) : TaskStatus()

    /**
     * The task failed at some point in the pipeline.
     *
     * @param message a user-presentable description of what went wrong.
     * @param cause an optional underlying throwable for logging/diagnostics.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : TaskStatus()

    /** The task was cancelled by the user before completion. */
    data object Cancelled : TaskStatus()
}
