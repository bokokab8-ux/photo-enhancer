package com.aienhancer.photoenhancer.domain.model

import java.util.UUID

/**
 * Core domain entity representing a single AI enhancement job.
 *
 * This is a pure Kotlin data class with zero Android framework dependencies
 * (no Uri, no Bitmap) so the domain layer stays platform-agnostic and testable
 * in plain JVM unit tests. The presentation layer is responsible for converting
 * platform types (android.net.Uri) to/from the String URIs used here.
 *
 * @param id a unique identifier for this task, generated at creation time.
 * @param sourceImageUri the URI (as a String) of the original, unedited image.
 * @param enhancementType which AI operation this task represents.
 * @param status the current lifecycle status of the task.
 * @param createdAtEpochMillis wall-clock creation timestamp, used for sorting history.
 */
data class ImageTask(
    val id: String = UUID.randomUUID().toString(),
    val sourceImageUri: String,
    val enhancementType: EnhancementType,
    val status: TaskStatus = TaskStatus.Idle,
    val createdAtEpochMillis: Long = System.currentTimeMillis()
) {
    /** Convenience flag for UI layers that just need a boolean "is this running". */
    val isProcessing: Boolean
        get() = status is TaskStatus.Processing

    /** Convenience flag for UI layers that just need a boolean "did this succeed". */
    val isComplete: Boolean
        get() = status is TaskStatus.Success
}
