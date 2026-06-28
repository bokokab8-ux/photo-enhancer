package com.aienhancer.photoenhancer.di

import com.aienhancer.photoenhancer.data.repository.ImageEnhancementRepositoryImpl
import com.aienhancer.photoenhancer.data.repository.RewardedAdRepositoryImpl
import com.aienhancer.photoenhancer.domain.repository.ImageEnhancementRepository
import com.aienhancer.photoenhancer.domain.repository.RewardedAdRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds domain-layer repository interfaces to their concrete data-layer
 * implementations.
 *
 * Using an abstract class with [Binds] functions (rather than a [dagger.Provides]
 * object module) is the idiomatic, more efficient Hilt pattern when the
 * implementation class itself can be constructor-injected - Dagger generates
 * less code and avoids an unnecessary factory method per binding.
 *
 * Installed in [SingletonComponent] so that both repository implementations -
 * which hold session-scoped or app-scoped mutable state (task history, ad
 * load state, unlocked-feature set) - live for the lifetime of the application
 * process rather than being recreated per-screen.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindImageEnhancementRepository(
        impl: ImageEnhancementRepositoryImpl
    ): ImageEnhancementRepository

    @Binds
    abstract fun bindRewardedAdRepository(
        impl: RewardedAdRepositoryImpl
    ): RewardedAdRepository
}
