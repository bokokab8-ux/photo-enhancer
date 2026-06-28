package com.aienhancer.photoenhancer.domain.model

/**
 * Identifies a feature that is gated behind a rewarded ad. Kept separate from
 * [EnhancementType] because, in principle, monetization gating could one day
 * apply to things that aren't enhancement operations (e.g. export-without-watermark).
 */
enum class RewardedFeature {
    UPSCALE_8X,
    BATCH_PROCESSING
}

/**
 * Represents the outcome of an attempt to show a rewarded ad.
 */
sealed class RewardAdResult {
    /** The user watched the ad to completion and earned the reward. */
    data class Granted(val rewardAmount: Int, val rewardType: String) : RewardAdResult()

    /** The ad was shown but the user dismissed it before earning the reward. */
    data object Dismissed : RewardAdResult()

    /** No ad was available to show (not loaded, network failure, etc). */
    data class NotAvailable(val reason: String) : RewardAdResult()

    /** The ad SDK reported a failure while showing the ad. */
    data class Failed(val message: String) : RewardAdResult()
}
