package com.aienhancer.photoenhancer.domain.usecase

import com.aienhancer.photoenhancer.domain.repository.RewardedAdRepository
import javax.inject.Inject

/**
 * Triggers preloading of the rewarded ad so it's ready the instant a user
 * taps a locked feature. Intended to be called from screen-entry effects
 * (e.g. LaunchedEffect on HomeScreen) rather than lazily on tap, to minimize
 * perceived latency.
 */
class PreloadRewardedAdUseCase @Inject constructor(
    private val rewardedAdRepository: RewardedAdRepository
) {
    suspend operator fun invoke() {
        rewardedAdRepository.preloadRewardedAd()
    }
}
