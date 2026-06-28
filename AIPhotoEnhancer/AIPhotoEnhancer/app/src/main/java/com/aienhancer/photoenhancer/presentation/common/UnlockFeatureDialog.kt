package com.aienhancer.photoenhancer.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Confirmation dialog shown before launching a rewarded ad to unlock a
 * premium feature (8x upscaling, batch processing). Separated from the
 * screen composables so both HomeScreen and EnhanceScreen can trigger the
 * exact same unlock UX without duplicating dialog markup.
 *
 * @param featureName the human-readable name of the feature being unlocked,
 *  interpolated into the dialog message.
 * @param isAdLoading whether an ad load/show is currently in progress, used
 *  to disable the confirm button and avoid double-taps.
 * @param onConfirm invoked when the user taps "Watch Ad".
 * @param onDismiss invoked when the user cancels or dismisses the dialog.
 */
@Composable
fun UnlockFeatureDialog(
    featureName: String,
    isAdLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Filled.PlayCircle, contentDescription = null)
        },
        title = { Text(text = "Unlock $featureName") },
        text = {
            Text(text = "Watch a short rewarded ad to unlock \"$featureName\" for this session.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isAdLoading
            ) {
                Text(text = if (isAdLoading) "Loading…" else "Watch Ad")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
