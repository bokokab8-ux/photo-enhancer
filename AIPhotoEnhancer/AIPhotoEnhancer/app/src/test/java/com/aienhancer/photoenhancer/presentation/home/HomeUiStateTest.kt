package com.aienhancer.photoenhancer.presentation.home

import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.RewardedFeature
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeUiStateTest {

    @Test
    fun `free feature is never locked`() {
        val state = HomeUiState(unlockedFeatures = emptySet())
        assertFalse(state.isFeatureLocked(EnhancementType.UPSCALE_4X))
        assertFalse(state.isFeatureLocked(EnhancementType.DENOISE))
        assertFalse(state.isFeatureLocked(EnhancementType.SHARPNESS))
    }

    @Test
    fun `premium feature is locked when not unlocked`() {
        val state = HomeUiState(unlockedFeatures = emptySet())
        assertTrue(state.isFeatureLocked(EnhancementType.UPSCALE_8X))
        assertTrue(state.isFeatureLocked(EnhancementType.BATCH_PROCESSING))
    }

    @Test
    fun `premium feature is unlocked once its reward is granted`() {
        val state = HomeUiState(unlockedFeatures = setOf(RewardedFeature.UPSCALE_8X))
        assertFalse(state.isFeatureLocked(EnhancementType.UPSCALE_8X))
        assertTrue(state.isFeatureLocked(EnhancementType.BATCH_PROCESSING))
    }

    @Test
    fun `mapping from enhancement type to rewarded feature is correct`() {
        assertTrue(EnhancementType.UPSCALE_8X.toRewardedFeatureOrNull() == RewardedFeature.UPSCALE_8X)
        assertTrue(EnhancementType.BATCH_PROCESSING.toRewardedFeatureOrNull() == RewardedFeature.BATCH_PROCESSING)
        assertTrue(EnhancementType.DENOISE.toRewardedFeatureOrNull() == null)
    }
}
