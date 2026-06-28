package com.aienhancer.photoenhancer.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Thin wrapper around the Google Mobile Ads SDK's [RewardedAd] API.
 *
 * This class is the *only* place in the codebase that touches AdMob types
 * directly. Everything above it (the repository implementation, use cases,
 * ViewModels) talks in terms of domain-level [com.aienhancer.photoenhancer.domain.model.RewardAdResult],
 * which keeps the vendor SDK swappable and keeps AdMob's callback-based API
 * from leaking into coroutine-based call sites.
 *
 * Uses the official AdMob *test* Ad Unit ID for rewarded ads. Replace
 * [TEST_REWARDED_AD_UNIT_ID] with your real ad unit ID before shipping to
 * production, and make sure to follow AdMob's policies around test devices
 * during development to avoid invalid traffic flags on your account.
 */
@Singleton
class AdMobRewardedAdSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AdMobRewardedAdSource"

        // Official Google-provided sample/test ad unit ID for rewarded ads.
        // Safe to use in development; always shows test creatives, never real ads.
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    @Volatile
    private var rewardedAd: RewardedAd? = null

    @Volatile
    private var isLoading: Boolean = false

    /**
     * Loads a rewarded ad into [rewardedAd] if one is not already loaded or
     * currently loading. Suspends until the load attempt finishes (success or
     * failure) so callers can sequence "preload, then optionally show" safely.
     */
    suspend fun load(): Boolean {
        if (rewardedAd != null) return true
        if (isLoading) return false

        return suspendCancellableCoroutine { continuation ->
            isLoading = true
            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(
                context,
                TEST_REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Rewarded ad loaded successfully.")
                        rewardedAd = ad
                        isLoading = false
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w(TAG, "Rewarded ad failed to load: ${adError.message}")
                        rewardedAd = null
                        isLoading = false
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            )
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null

    /**
     * Shows the currently loaded rewarded ad on top of [activity], suspending
     * until the user earns the reward, dismisses the ad, or showing fails.
     *
     * @return a [RewardOutcome] describing what happened. The loaded ad
     * reference is always cleared after this call returns, since a [RewardedAd]
     * instance is single-use and must be reloaded for next time.
     */
    suspend fun show(activity: Activity): RewardOutcome {
        val ad = rewardedAd ?: return RewardOutcome.NotAvailable

        return suspendCancellableCoroutine { continuation ->
            var rewardEarned = false
            var earnedAmount = 0
            var earnedType = ""

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    val outcome = if (rewardEarned) {
                        RewardOutcome.Granted(earnedAmount, earnedType)
                    } else {
                        RewardOutcome.Dismissed
                    }
                    if (continuation.isActive) continuation.resume(outcome)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                    if (continuation.isActive) {
                        continuation.resume(RewardOutcome.Failed(adError.message))
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed full screen content.")
                }
            }

            ad.show(activity) { rewardItem: RewardItem ->
                rewardEarned = true
                earnedAmount = rewardItem.amount
                earnedType = rewardItem.type
                Log.d(TAG, "User earned reward: $earnedAmount $earnedType")
            }
        }
    }

    /** Outcome of attempting to show a rewarded ad, decoupled from GMA SDK types. */
    sealed class RewardOutcome {
        data class Granted(val amount: Int, val type: String) : RewardOutcome()
        data object Dismissed : RewardOutcome()
        data object NotAvailable : RewardOutcome()
        data class Failed(val message: String) : RewardOutcome()
    }
}
