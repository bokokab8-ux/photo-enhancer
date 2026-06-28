package com.aienhancer.photoenhancer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.RewardAdResult
import com.aienhancer.photoenhancer.domain.usecase.PreloadRewardedAdUseCase
import com.aienhancer.photoenhancer.domain.usecase.UnlockPremiumFeatureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for [HomeScreen].
 *
 * Owns:
 *  - The currently selected source image (from gallery or camera).
 *  - The rewarded-ad unlock flow for premium feature cards, including
 *    preloading the ad as early as possible and surfacing every possible
 *    [RewardAdResult] outcome (granted / dismissed / not available / failed)
 *    as appropriate user-facing feedback rather than silently no-op-ing on
 *    anything but success.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val unlockPremiumFeatureUseCase: UnlockPremiumFeatureUseCase,
    private val preloadRewardedAdUseCase: PreloadRewardedAdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        preloadAd()
    }

    /** Preloads a rewarded ad so it's instant to show when a locked card is tapped. */
    private fun preloadAd() {
        viewModelScope.launch {
            preloadRewardedAdUseCase()
        }
    }

    /** Called when the user picks an image from the gallery or finishes a camera capture. */
    fun onImageSelected(uri: String) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun clearSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null) }
    }

    /**
     * Called when the user taps a feature card. If the feature is free, the
     * caller (the screen) should navigate immediately - this function only
     * needs to run for gated features, to kick off the unlock confirmation flow.
     */
    fun onFeatureCardTapped(enhancementType: EnhancementType) {
        if (!_uiState.value.isFeatureLocked(enhancementType)) return
        _uiState.update { it.copy(pendingUnlockRequest = enhancementType) }
    }

    fun dismissUnlockDialog() {
        _uiState.update { it.copy(pendingUnlockRequest = null) }
    }

    /** Called when the user confirms "Watch Ad" in the unlock dialog. */
    fun onWatchAdConfirmed() {
        val feature = _uiState.value.pendingUnlockRequest?.toRewardedFeatureOrNull() ?: run {
            dismissUnlockDialog()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAdLoading = true) }
            val result = unlockPremiumFeatureUseCase(feature)
            _uiState.update { current ->
                when (result) {
                    is RewardAdResult.Granted -> current.copy(
                        unlockedFeatures = current.unlockedFeatures + feature,
                        pendingUnlockRequest = null,
                        isAdLoading = false,
                        snackbarMessage = "Feature unlocked!"
                    )
                    is RewardAdResult.Dismissed -> current.copy(
                        isAdLoading = false,
                        snackbarMessage = "Watch the full ad to unlock this feature."
                    )
                    is RewardAdResult.NotAvailable -> current.copy(
                        isAdLoading = false,
                        snackbarMessage = result.reason
                    )
                    is RewardAdResult.Failed -> current.copy(
                        isAdLoading = false,
                        snackbarMessage = "Ad failed: ${result.message}"
                    )
                }
            }
            // Try to immediately warm up the next ad for the following request.
            preloadAd()
        }
    }

    fun consumeSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
