# Add project specific ProGuard rules here.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep domain models (used in navigation arguments / serialization)
-keep class com.aienhancer.photoenhancer.domain.model.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Coroutines
-dontwarn kotlinx.coroutines.flow.**
