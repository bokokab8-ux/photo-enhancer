package com.aienhancer.photoenhancer.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A circular determinate progress indicator showing AI pipeline progress,
 * paired with the current stage label (e.g. "Upscaling resolution") and a
 * percentage readout in the center.
 *
 * The progress value is animated with [animateFloatAsState] so that rapid,
 * small emissions from the underlying Flow (e.g. every ~5-10% as the
 * simulated pipeline advances) render as a smooth sweep rather than visibly
 * discrete jumps.
 *
 * @param progress current completion fraction, 0f..1f.
 * @param stageLabel human-readable description of what the pipeline is doing right now.
 */
@Composable
fun ProcessingIndicator(
    progress: Float,
    stageLabel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "enhancement_progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(120.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = stageLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
