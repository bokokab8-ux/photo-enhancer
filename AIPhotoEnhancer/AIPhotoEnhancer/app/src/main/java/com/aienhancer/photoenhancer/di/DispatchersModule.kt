package com.aienhancer.photoenhancer.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/** Qualifier for the IO-bound coroutine dispatcher (disk/network work). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Qualifier for the CPU-bound default coroutine dispatcher (image processing math). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** Qualifier for the main/UI coroutine dispatcher. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Provides named [CoroutineDispatcher] instances for injection.
 *
 * Centralizing dispatcher provisioning here (instead of hardcoding
 * `Dispatchers.IO` inline throughout the data layer) makes the dispatcher
 * choice swappable in tests - a test module can replace these bindings with
 * `kotlinx-coroutines-test`'s `StandardTestDispatcher` without touching
 * production code.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
