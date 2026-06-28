package com.aienhancer.photoenhancer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aienhancer.photoenhancer.presentation.navigation.PhotoEnhancerNavHost
import com.aienhancer.photoenhancer.ui.theme.AIPhotoEnhancerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The application's single Activity, hosting the entire Jetpack Compose UI
 * tree via [setContent]. Annotated with [AndroidEntryPoint] so Hilt can inject
 * dependencies into this Activity (and, transitively, make the Activity-scoped
 * Hilt component available to any Hilt-aware composables/ViewModels created
 * beneath it, such as every screen reached via [PhotoEnhancerNavHost]).
 *
 * Calls [enableEdgeToEdge] so the app draws behind the system status and
 * navigation bars, matching modern Material 3 visual guidance; per-screen
 * composables are responsible for their own inner padding via Scaffold.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhotoEnhancerApp()
        }
    }
}

@Composable
private fun PhotoEnhancerApp() {
    AIPhotoEnhancerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            PhotoEnhancerNavHost(navController = navController)
        }
    }
}
