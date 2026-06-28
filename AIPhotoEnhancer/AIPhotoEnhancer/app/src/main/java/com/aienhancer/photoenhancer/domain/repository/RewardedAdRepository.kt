package com.aienhancer.photoenhancer.domain.repository

import com.aienhancer.photoenhancer.domain.model.RewardAdResult
import com.aienhancer.photoenhancer.domain.model.RewardedFeature
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for monetization via rewarded interstitial ads.
 *
 * This abstraction deliberately knows nothing about AdMob/GMA SDK types -
 * those live entirely inside the data-layer implementation - so that the
 * domain and presentation layers are not coupled to a specific ad vendor and
 * could be swapped (e.g. for a different network or a paid-unlock IAP flow)
 * without touching ViewModels or use cases.
 */
interface RewardedAdRepository {

    /**
     * Preloads a rewarded ad into memory so it's ready to show instantly when
     * the user requests an unlock. Safe to call multiple times; implementations
     * should no-op if an ad is already loaded or a load is already in flight.
     */
    suspend fun preloadRewardedAd()

    /**
     * Shows the rewarded ad (if loaded) and suspends until the user has either
     * earned the reward, dismissed the ad, or the SDK reports a failure.
     */
    suspend fun showRewardedAd(): RewardAdResult

    /**
     * Whether [feature] has already been unlocked for the current app session
     * (i.e. the user previously watched a rewarded ad for it).
     */
    fun isFeatureUnlocked(feature: RewardedFeature): Boolean

    /**
     * Observes the live set of features unlocked in this session, so UI such as
     * the feature grid can reactively remove "PRO" badges the moment a reward
     * is granted.
     */
    fun observeUnlockedFeatures(): Flow<Set<RewardedFeature>>

    /** Marks [feature] as unlocked for the remainder of the current app session. */
    fun unlockFeature(feature: RewardedFeature)
}
