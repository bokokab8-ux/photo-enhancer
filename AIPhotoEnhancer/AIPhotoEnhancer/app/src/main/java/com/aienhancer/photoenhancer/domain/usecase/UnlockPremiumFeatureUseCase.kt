package com.aienhancer.photoenhancer.domain.usecase

import com.aienhancer.photoenhancer.domain.model.RewardAdResult
import com.aienhancer.photoenhancer.domain.model.RewardedFeature
import com.aienhancer.photoenhancer.domain.repository.RewardedAdRepository
import javax.inject.Inject

/**
 * Encapsulates the "watch a rewarded ad to unlock a premium feature" flow.
 *
 * This is the single source of truth for what counts as a successful unlock:
 * only [RewardAdResult.Granted] actually flips the feature's unlocked state.
 * Both [EnhanceViewModel] and [HomeViewModel] depend on this use case rather
 * than talking to [RewardedAdRepository] directly, so the unlock business rule
 * cannot drift between screens.
 */
class UnlockPremiumFeatureUseCase @Inject constructor(
    private val rewardedAdRepository: RewardedAdRepository
) {
    /**
     * Shows a rewarded ad and, if the user earns the reward, marks [feature] as
     * unlocked for the session. Returns the raw [RewardAdResult] so the caller
     * (typically a ViewModel) can show appropriate UI feedback for every outcome,
     * not just the success path.
     */
    suspend operator fun invoke(feature: RewardedFeature): RewardAdResult {
        if (rewardedAdRepository.isFeatureUnlocked(feature)) {
            // Already unlocked this session - no need to show another ad.
            return RewardAdResult.Granted(rewardAmount = 1, rewardType = "unlock")
        }

        val result = rewardedAdRepository.showRewardedAd()
        if (result is RewardAdResult.Granted) {
            rewardedAdRepository.unlockFeature(feature)
        }
        return result
    }

    fun isUnlocked(feature: RewardedFeature): Boolean =
        rewardedAdRepository.isFeatureUnlocked(feature)
}
