package com.aienhancer.photoenhancer.presentation.enhance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aienhancer.photoenhancer.presentation.common.BeforeAfterSlider
import com.aienhancer.photoenhancer.presentation.common.ProcessingIndicator

/**
 * The enhancement workflow screen: shows the source image while Idle, a
 * circular progress readout while Processing, and an interactive Before/After
 * comparison slider plus export action once the pipeline reaches Success.
 * On Error, shows a retry affordance.
 *
 * @param onNavigateBack invoked when the user taps the back arrow in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: EnhanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        val successState = uiState as? EnhanceUiState.Success
        successState?.exportedMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Enhance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            when (val state = uiState) {
                is EnhanceUiState.Idle -> IdleContent(
                    state = state,
                    onStartEnhancing = viewModel::startEnhancement
                )

                is EnhanceUiState.Processing -> ProcessingContent(state = state)

                is EnhanceUiState.Success -> SuccessContent(
                    state = state,
                    onExport = viewModel::exportToGallery
                )

                is EnhanceUiState.Error -> ErrorContent(
                    state = state,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    state: EnhanceUiState.Idle,
    onStartEnhancing: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = state.sourceImageUri,
            contentDescription = "Source image to enhance",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Ready to apply ${state.enhancementType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onStartEnhancing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = "Start Enhancing")
        }
    }
}

@Composable
private fun ProcessingContent(state: EnhanceUiState.Processing) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
        ) {
            AsyncImage(
                model = state.sourceImageUri,
                contentDescription = "Image being enhanced",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        ProcessingIndicator(
            progress = state.progress,
            stageLabel = state.stageLabel,
            modifier = Modifier.padding(top = 32.dp)
        )

        Text(
            text = "Applying ${state.enhancementType.displayName} - please keep the app open",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun SuccessContent(
    state: EnhanceUiState.Success,
    onExport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BeforeAfterSlider(
            beforeImageUri = state.sourceImageUri,
            afterImageUri = state.enhancedImageUri,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Text(
            text = "${state.enhancementType.displayName} complete in ${state.processingDurationMs / 1000.0}s",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Button(
            onClick = onExport,
            enabled = !state.isExporting,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = if (state.isExporting) "Exporting…" else "Export")
        }
    }
}

@Composable
private fun ErrorContent(
    state: EnhanceUiState.Error,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = state.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Retry")
        }
    }
}
