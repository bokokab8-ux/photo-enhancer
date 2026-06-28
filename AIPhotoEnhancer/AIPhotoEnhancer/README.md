# AI Photo Enhancer

A production-grade Android application demonstrating Clean Architecture, MVVM,
Jetpack Compose (Material 3), Dagger Hilt, and AdMob rewarded-ad monetization.

## Architecture

The codebase is split into three strictly separated layers:

```
domain/         Pure Kotlin, zero Android dependencies. Entities, repository
                interfaces, and use cases. Fully unit-testable on the plain JVM.

data/           Concrete repository implementations. Owns all Android
                framework types (Context, Uri, Bitmap, MediaStore) and the
                AdMob SDK integration. Implements the domain's repository
                interfaces; never referenced directly by the presentation layer.

presentation/   Jetpack Compose screens + ViewModels (MVVM). Each screen has a
                dedicated UiState (sealed class for Enhance, data class for
                Home) and a HiltViewModel that exposes a single StateFlow<UiState>.
```

Dependency direction is enforced one-way: `presentation -> domain <- data`.
Nothing in `domain` imports from `data` or `presentation`.

## Key Components

| File | Purpose |
|---|---|
| `domain/model/ImageTask.kt` | Core entity representing one enhancement job |
| `domain/model/TaskStatus.kt` | Sealed class: Idle / Processing / Success / Error / Cancelled |
| `data/repository/ImageEnhancementRepositoryImpl.kt` | Simulated staged AI pipeline using `callbackFlow` |
| `data/ads/AdMobRewardedAdSource.kt` | Thin AdMob SDK wrapper, isolated from the rest of the app |
| `presentation/common/BeforeAfterSlider.kt` | Custom gesture-driven before/after comparison widget |
| `presentation/enhance/EnhanceViewModel.kt` | Drives the Idle→Processing→Success/Error state machine |
| `presentation/home/HomeViewModel.kt` | Image selection + rewarded-ad unlock flow |
| `di/AppModule.kt` | Hilt bindings: domain interfaces → data implementations |

## Monetization

`UPSCALE_8X` and `BATCH_PROCESSING` are gated behind a rewarded interstitial ad
(`UnlockPremiumFeatureUseCase`). The app ships wired to Google's official
**test** Ad Unit ID (`ca-app-pub-3940256099942544/5224354917`) and **test**
App ID (`ca-app-pub-3940256099942544~3347511713`) — replace both in
`app/build.gradle.kts` (`admobAppId` manifest placeholder) and
`AdMobRewardedAdSource.TEST_REWARDED_AD_UNIT_ID` before submitting to
production. Unlocks are session-scoped (in-memory); see the KDoc on
`RewardedAdRepositoryImpl` for notes on adding persistence.

## Building

1. Open the project root in Android Studio (Ladybug or newer recommended).
2. Let Gradle sync — all dependencies are declared in `gradle/libs.versions.toml`.
3. Run on a device/emulator with API 26+.

No API keys are required to build and run; AdMob test ads work out of the box
in debug builds without any additional configuration.

## Testing

Unit tests live under `app/src/test` and cover the domain use cases and
presentation-layer state logic using JUnit4, MockK, Turbine, and
kotlinx-coroutines-test:

```
./gradlew testDebugUnitTest
```

## Notable Design Decisions

- **`callbackFlow` for the pipeline**: lets the data layer emit fine-grained
  `Processing(progress, stage)` updates while still supporting cooperative
  cancellation if the user navigates away mid-enhancement.
- **No AdMob types above the data layer**: `RewardAdResult` is a domain-owned
  sealed class; swapping ad networks only touches `data/ads/` and
  `RewardedAdRepositoryImpl`.
- **`CurrentActivityProvider`**: avoids passing `Activity` references through
  ViewModels/use cases, which would violate MVVM's "ViewModels don't hold
  Android UI references" rule, while still satisfying the AdMob SDK's
  requirement that `RewardedAd.show()` be called with an `Activity`.
