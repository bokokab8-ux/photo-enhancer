package com.aienhancer.photoenhancer.data.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks the current foreground [Activity] via [Application.ActivityLifecycleCallbacks]
 * so that the data layer can show a full-screen rewarded ad without ViewModels
 * or repositories needing to hold a direct, potentially leaking, Activity reference.
 *
 * A [WeakReference] is used as a defense-in-depth measure: even though we clear
 * the reference in [onActivityDestroyed], the weak reference ensures we can
 * never accidentally keep an Activity alive past its natural lifecycle.
 */
@Singleton
class CurrentActivityProvider @Inject constructor() : Application.ActivityLifecycleCallbacks {

    private var currentActivityRef: WeakReference<Activity>? = null

    val currentActivity: Activity?
        get() = currentActivityRef?.get()

    override fun onActivityResumed(activity: Activity) {
        currentActivityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}
