package com.aienhancer.photoenhancer.domain.usecase

import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.ImageTask
import com.aienhancer.photoenhancer.domain.repository.ImageEnhancementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Orchestrates running the AI enhancement pipeline for a single image.
 *
 * Kept as a thin wrapper around the repository for now, but exists as its own
 * use case (rather than calling the repository directly from the ViewModel) so
 * that any future cross-cutting business rule - e.g. "always downscale images
 * larger than 4096px before enhancing to save memory" or "log analytics event
 * on every enhancement start" - has a single, testable place to live without
 * the ViewModel needing to know about it.
 */
class EnhanceImageUseCase @Inject constructor(
    private val repository: ImageEnhancementRepository
) {
    operator fun invoke(
        sourceImageUri: String,
        enhancementType: EnhancementType
    ): Flow<ImageTask> {
        require(sourceImageUri.isNotBlank()) { "sourceImageUri must not be blank" }
        return repository.enhanceImage(sourceImageUri, enhancementType)
    }
}
