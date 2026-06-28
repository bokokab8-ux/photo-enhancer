package com.aienhancer.photoenhancer.domain.usecase

import com.aienhancer.photoenhancer.domain.repository.ImageEnhancementRepository
import javax.inject.Inject

/**
 * Saves a finished enhancement result into the device gallery.
 */
class ExportEnhancedImageUseCase @Inject constructor(
    private val repository: ImageEnhancementRepository
) {
    suspend operator fun invoke(enhancedImageUri: String): Result<String> {
        if (enhancedImageUri.isBlank()) {
            return Result.failure(IllegalArgumentException("enhancedImageUri must not be blank"))
        }
        return repository.exportToGallery(enhancedImageUri)
    }
}
