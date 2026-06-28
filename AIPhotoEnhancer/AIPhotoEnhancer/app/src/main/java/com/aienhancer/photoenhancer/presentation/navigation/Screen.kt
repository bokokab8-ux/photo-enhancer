package com.aienhancer.photoenhancer.presentation.navigation

/**
 * Type-safe navigation destinations for the app's [androidx.navigation.NavHost].
 *
 * Each destination knows how to build both its route *pattern* (used when
 * registering the composable with NavHost) and a concrete route *instance*
 * (used when navigating to it with actual argument values), which avoids the
 * stringly-typed bugs that come from hand-formatting route strings at every
 * call site.
 */
sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object Enhance : Screen("enhance/{encodedImageUri}/{enhancementType}") {
        const val ARG_IMAGE_URI = "encodedImageUri"
        const val ARG_ENHANCEMENT_TYPE = "enhancementType"

        /**
         * Builds a concrete, navigable route for a specific image + enhancement type.
         * The image URI is URL-encoded since raw content:// URIs contain characters
         * (':', '/') that are not safe to place directly inside a nav-graph path segment.
         */
        fun createRoute(encodedImageUri: String, enhancementType: String): String =
            "enhance/$encodedImageUri/$enhancementType"
    }
}
