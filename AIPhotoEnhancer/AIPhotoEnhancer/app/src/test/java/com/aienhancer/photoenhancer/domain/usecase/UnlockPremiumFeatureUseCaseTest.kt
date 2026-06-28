package com.aienhancer.photoenhancer.domain.usecase

import com.aienhancer.photoenhancer.domain.model.RewardAdResult
import com.aienhancer.photoenhancer.domain.model.RewardedFeature
import com.aienhancer.photoenhancer.domain.repository.RewardedAdRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnlockPremiumFeatureUseCaseTest {

    private lateinit var rewardedAdRepository: RewardedAdRepository
    private lateinit var useCase: UnlockPremiumFeatureUseCase

    @Before
    fun setUp() {
        rewardedAdRepository = mockk()
        useCase = UnlockPremiumFeatureUseCase(rewardedAdRepository)
    }

    @Test
    fun `already unlocked feature short-circuits without showing an ad`() = runTest {
        every { rewardedAdRepository.isFeatureUnlocked(RewardedFeature.UPSCALE_8X) } returns true

        val result = useCase(RewardedFeature.UPSCALE_8X)

        assertTrue(result is RewardAdResult.Granted)
        coVerify(exactly = 0) { rewardedAdRepository.showRewardedAd() }
    }

    @Test
    fun `granted reward unlocks the feature`() = runTest {
        every { rewardedAdRepository.isFeatureUnlocked(RewardedFeature.UPSCALE_8X) } returns false
        coEvery { rewardedAdRepository.showRewardedAd() } returns RewardAdResult.Granted(1, "unlock")
        every { rewardedAdRepository.unlockFeature(RewardedFeature.UPSCALE_8X) } returns Unit

        val result = useCase(RewardedFeature.UPSCALE_8X)

        assertEquals(RewardAdResult.Granted(1, "unlock"), result)
        coVerify(exactly = 1) { rewardedAdRepository.unlockFeature(RewardedFeature.UPSCALE_8X) }
    }

    @Test
    fun `dismissed ad does not unlock the feature`() = runTest {
        every { rewardedAdRepository.isFeatureUnlocked(RewardedFeature.BATCH_PROCESSING) } returns false
        coEvery { rewardedAdRepository.showRewardedAd() } returns RewardAdResult.Dismissed

        val result = useCase(RewardedFeature.BATCH_PROCESSING)

        assertEquals(RewardAdResult.Dismissed, result)
        coVerify(exactly = 0) { rewardedAdRepository.unlockFeature(any()) }
    }

    @Test
    fun `failed ad surfaces the failure without unlocking`() = runTest {
        every { rewardedAdRepository.isFeatureUnlocked(RewardedFeature.UPSCALE_8X) } returns false
        coEvery { rewardedAdRepository.showRewardedAd() } returns RewardAdResult.Failed("network error")

        val result = useCase(RewardedFeature.UPSCALE_8X)

        assertEquals(RewardAdResult.Failed("network error"), result)
        coVerify(exactly = 0) { rewardedAdRepository.unlockFeature(any()) }
    }
}
