package com.aienhancer.photoenhancer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.presentation.enhance.EnhanceScreen
import com.aienhancer.photoenhancer.presentation.home.HomeScreen
import java.net.URLEncoder

/**
 * The app's complete navigation graph, wired up with Navigation Compose.
 *
 * Two destinations: [Screen.Home] (the entry point) and [Screen.Enhance],
 * which receives the selected image URI and chosen [EnhancementType] as
 * navigation arguments. The image URI is URL-encoded before being placed in
 * the route string since raw content:// URIs contain reserved path characters
 * (':' and '/') that would otherwise corrupt the route segment, and decoded
 * again inside [com.aienhancer.photoenhancer.presentation.enhance.EnhanceViewModel].
 */
@Composable
fun PhotoEnhancerNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToEnhance = { imageUri, enhancementType ->
                    val encodedUri = URLEncoder.encode(imageUri, "UTF-8")
                    navController.navigate(
                        Screen.Enhance.createRoute(encodedUri, enhancementType.name)
                    )
                }
            )
        }

        composable(
            route = Screen.Enhance.route,
            arguments = listOf(
                navArgument(Screen.Enhance.ARG_IMAGE_URI) { type = NavType.StringType },
                navArgument(Screen.Enhance.ARG_ENHANCEMENT_TYPE) { type = NavType.StringType }
            )
        ) {
            EnhanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
