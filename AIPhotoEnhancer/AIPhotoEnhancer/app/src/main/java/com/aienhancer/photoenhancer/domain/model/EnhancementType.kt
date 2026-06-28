package com.aienhancer.photoenhancer.domain.model

/**
 * The set of AI enhancement operations the app can perform on an image.
 *
 * @param displayName human-readable label shown in the UI.
 * @param isPremium whether this feature requires a rewarded-ad unlock (or
 *  subscription, in a future iteration) before it can be used.
 * @param basePipelineDurationMs a rough simulated baseline duration used by the
 *  fake data-layer pipeline to make demo processing feel proportionate to the
 *  complexity of the operation (8x upscaling takes longer than denoise, etc).
 */
enum class EnhancementType(
    val displayName: String,
    val isPremium: Boolean,
    val basePipelineDurationMs: Long
) {
    UPSCALE_4X(
        displayName = "Upscale 4x",
        isPremium = false,
        basePipelineDurationMs = 3500L
    ),
    UPSCALE_8X(
        displayName = "Upscale 8x",
        isPremium = true,
        basePipelineDurationMs = 6000L
    ),
    DENOISE(
        displayName = "Denoise",
        isPremium = false,
        basePipelineDurationMs = 2500L
    ),
    SHARPNESS(
        displayName = "Sharpness",
        isPremium = false,
        basePipelineDurationMs = 2000L
    ),
    BATCH_PROCESSING(
        displayName = "Batch Processing",
        isPremium = true,
        basePipelineDurationMs = 8000L
    )
}
