package com.aienhancer.photoenhancer.presentation.home

import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.RewardedFeature

/**
 * Immutable UI state for [HomeScreen], produced by [HomeViewModel].
 *
 * @param selectedImageUri the most recently picked/captured image, as a String
 *  URI, or null if the user hasn't chosen one yet. The "Enhance" navigation
 *  action is only enabled once this is non-null.
 * @param unlockedFeatures the set of premium features unlocked this session,
 *  used to decide whether a feature card shows a lock badge.
 * @param pendingUnlockRequest if non-null, the feature the user just tapped
 *  while it was still locked - drives whether [UnlockFeatureDialog] is shown.
 * @param isAdLoading true while a rewarded ad is being loaded/shown, used to
 *  disable the dialog's confirm button to prevent double taps.
 * @param snackbarMessage a one-shot message to surface (e.g. "Ad not ready"),
 *  cleared by the screen after being shown.
 */
data class HomeUiState(
    val selectedImageUri: String? = null,
    val unlockedFeatures: Set<RewardedFeature> = emptySet(),
    val pendingUnlockRequest: EnhancementType? = null,
    val isAdLoading: Boolean = false,
    val snackbarMessage: String? = null
) {
    fun isFeatureLocked(enhancementType: EnhancementType): Boolean {
        if (!enhancementType.isPremium) return false
        val rewardedFeature = enhancementType.toRewardedFeatureOrNull() ?: return false
        return rewardedFeature !in unlockedFeatures
    }
}

/**
 * Maps an [EnhancementType] to its corresponding [RewardedFeature] gate, or
 * null if the type isn't gated behind a rewarded ad at all. Kept as an
 * extension function (rather than a field on the enum itself) since the
 * monetization mapping is a presentation/business concern, not an intrinsic
 * property of the enhancement operation.
 */
fun EnhancementType.toRewardedFeatureOrNull(): RewardedFeature? = when (this) {
    EnhancementType.UPSCALE_8X -> RewardedFeature.UPSCALE_8X
    EnhancementType.BATCH_PROCESSING -> RewardedFeature.BATCH_PROCESSING
    else -> null
}
