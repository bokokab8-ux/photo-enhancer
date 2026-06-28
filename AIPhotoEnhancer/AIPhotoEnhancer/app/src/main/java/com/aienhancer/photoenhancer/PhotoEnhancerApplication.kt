package com.aienhancer.photoenhancer

import android.app.Application
import com.aienhancer.photoenhancer.data.ads.CurrentActivityProvider
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application entry point. Annotated with [HiltAndroidApp] to trigger Hilt's
 * code generation for the dependency graph used throughout the app.
 *
 * Responsible for two pieces of app-wide bootstrapping:
 *  1. Initializing the Google Mobile Ads SDK once, as early as possible, so
 *     the first rewarded-ad preload request (triggered from HomeScreen) does
 *     not race the SDK's own internal setup.
 *  2. Registering [CurrentActivityProvider] as an activity lifecycle callback
 *     so the data layer can show full-screen ads without holding a direct
 *     reference to any Activity itself.
 */
@HiltAndroidApp
class PhotoEnhancerApplication : Application() {

    @Inject
    lateinit var currentActivityProvider: CurrentActivityProvider

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(currentActivityProvider)
        applicationScope.launch {
            MobileAds.initialize(this@PhotoEnhancerApplication) { initializationStatus ->
                // Initialization status per ad adapter is available here if you need
                // to log it for diagnostics: initializationStatus.adapterStatusMap
            }
        }
    }
}
