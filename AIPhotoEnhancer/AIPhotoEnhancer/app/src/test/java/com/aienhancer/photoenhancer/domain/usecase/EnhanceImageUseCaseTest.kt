package com.aienhancer.photoenhancer.domain.usecase

import app.cash.turbine.test
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.ImageTask
import com.aienhancer.photoenhancer.domain.model.TaskStatus
import com.aienhancer.photoenhancer.domain.repository.ImageEnhancementRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class EnhanceImageUseCaseTest {

    private lateinit var repository: ImageEnhancementRepository
    private lateinit var useCase: EnhanceImageUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = EnhanceImageUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository and emits all statuses`() = runTest {
        val sourceUri = "content://fake/image.jpg"
        val type = EnhancementType.UPSCALE_4X

        val expectedTask = ImageTask(
            sourceImageUri = sourceUri,
            enhancementType = type,
            status = TaskStatus.Success(outputUri = "file://out.jpg", processingDurationMs = 100L)
        )

        every { repository.enhanceImage(sourceUri, type) } returns flowOf(expectedTask)

        useCase(sourceUri, type).test {
            val emitted = awaitItem()
            assertEquals(expectedTask, emitted)
            awaitComplete()
        }
    }

    @Test
    fun `invoke throws for blank source uri`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase("", EnhancementType.DENOISE)
        }
    }
}
