package com.aienhancer.photoenhancer.data.repository

import com.aienhancer.photoenhancer.data.ads.AdMobRewardedAdSource
import com.aienhancer.photoenhancer.data.ads.CurrentActivityProvider
import com.aienhancer.photoenhancer.domain.model.RewardAdResult
import com.aienhancer.photoenhancer.domain.model.RewardedFeature
import com.aienhancer.photoenhancer.domain.repository.RewardedAdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [RewardedAdRepository] backed by AdMob rewarded interstitial
 * ads via [AdMobRewardedAdSource].
 *
 * Unlocked features are tracked in-memory (per app session) using a
 * [MutableStateFlow]. This intentionally does *not* persist across app restarts
 * in this scaffold - a production app would likely back this with DataStore or
 * a server-verified entitlement instead, especially if unlocks should survive
 * reinstalls or sync across devices. Swapping in persistence only requires
 * changing this class; the domain contract already supports it since
 * [observeUnlockedFeatures] is exposed as a Flow.
 */
@Singleton
class RewardedAdRepositoryImpl @Inject constructor(
    private val adSource: AdMobRewardedAdSource,
    private val currentActivityProvider: CurrentActivityProvider
) : RewardedAdRepository {

    private val unlockedFeatures: MutableStateFlow<Set<RewardedFeature>> =
        MutableStateFlow(emptySet())

    override suspend fun preloadRewardedAd() {
        if (!adSource.isAdLoaded()) {
            adSource.load()
        }
    }

    override suspend fun showRewardedAd(): RewardAdResult {
        // Ensure an ad is loaded; if preload hasn't happened yet or failed, try once more.
        if (!adSource.isAdLoaded()) {
            val loaded = adSource.load()
            if (!loaded) {
                return RewardAdResult.NotAvailable(
                    reason = "Ad failed to load. Check your network connection and try again."
                )
            }
        }

        val activity = currentActivityProvider.currentActivity
            ?: return RewardAdResult.NotAvailable(reason = "No active screen to show the ad on.")

        return when (val outcome = adSource.show(activity)) {
            is AdMobRewardedAdSource.RewardOutcome.Granted ->
                RewardAdResult.Granted(rewardAmount = outcome.amount, rewardType = outcome.type)

            is AdMobRewardedAdSource.RewardOutcome.Dismissed ->
                RewardAdResult.Dismissed

            is AdMobRewardedAdSource.RewardOutcome.NotAvailable ->
                RewardAdResult.NotAvailable(reason = "Ad is not ready yet.")

            is AdMobRewardedAdSource.RewardOutcome.Failed ->
                RewardAdResult.Failed(message = outcome.message)
        }
    }

    override fun isFeatureUnlocked(feature: RewardedFeature): Boolean =
        unlockedFeatures.value.contains(feature)

    override fun observeUnlockedFeatures(): StateFlow<Set<RewardedFeature>> =
        unlockedFeatures

    override fun unlockFeature(feature: RewardedFeature) {
        unlockedFeatures.update { current -> current + feature }
    }
}
