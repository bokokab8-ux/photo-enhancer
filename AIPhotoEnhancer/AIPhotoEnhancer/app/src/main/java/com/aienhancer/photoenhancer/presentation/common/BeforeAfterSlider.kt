package com.aienhancer.photoenhancer.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * An interactive, gesture-driven before/after image comparison slider.
 *
 * Visually, the "after" image is drawn full-bleed beneath a clip mask that
 * only reveals the "before" image up to the current handle position; dragging
 * left/right (or tapping anywhere in the bounds) moves the dividing line,
 * letting the user "wipe" between the two images to compare them directly.
 *
 * This is implemented at the Canvas/pointerInput level (rather than, say,
 * two overlapping Image composables with manually-computed width modifiers)
 * so the divider line, handle, and drag hit-testing all share one source of
 * truth for geometry and stay perfectly in sync during fast drags.
 *
 * @param beforeImageUri the original, unenhanced image to show on the left side
 *  of the divider.
 * @param afterImageUri the enhanced image to show on the right side of the divider.
 * @param modifier modifier applied to the outer container; should typically
 *  constrain both width and height (e.g. via `.fillMaxWidth().aspectRatio(...)`).
 * @param initialPosition the starting position of the divider, in the range
 *  0f (fully showing "after") to 1f (fully showing "before"). Defaults to the
 *  midpoint.
 */
@Composable
fun BeforeAfterSlider(
    beforeImageUri: String,
    afterImageUri: String,
    modifier: Modifier = Modifier,
    initialPosition: Float = 0.5f
) {
    // sliderPosition represents how much of the BEFORE image is revealed,
    // from the left edge, as a fraction of total width (0f..1f).
    var sliderPosition by remember { mutableFloatStateOf(initialPosition.coerceIn(0f, 1f)) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(widthPx) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val newPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                        sliderPosition = newPosition
                    }
                }
                .pointerInput(widthPx) {
                    detectTapGestures { tapOffset ->
                        val newPosition = (tapOffset.x / size.width).coerceIn(0f, 1f)
                        sliderPosition = newPosition
                    }
                }
        ) {
            // AFTER image: drawn full-bleed as the base layer.
            AsyncImage(
                model = afterImageUri,
                contentDescription = "Enhanced image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // BEFORE image: clipped to only the region left of the divider,
            // drawn on top of the AFTER image.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        BeforeImageRevealShape(revealFraction = sliderPosition)
                    )
            ) {
                AsyncImage(
                    model = beforeImageUri,
                    contentDescription = "Original image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Divider line + drag handle, positioned at the current slider fraction.
            DividerAndHandle(
                sliderPosition = sliderPosition,
                containerWidthPx = widthPx
            )

            // Labels
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "BEFORE",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "AFTER",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Draws the vertical divider line and the circular drag handle on top of the
 * image stack, at the x-position corresponding to [sliderPosition].
 */
@Composable
private fun DividerAndHandle(
    sliderPosition: Float,
    containerWidthPx: Float
) {
    val handleColor = Color.White
    val lineColor = Color.White

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val xPosition = size.width * sliderPosition
            drawLine(
                color = lineColor,
                start = Offset(xPosition, 0f),
                end = Offset(xPosition, size.height),
                strokeWidth = 4f
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val xPx = (containerWidthPx * sliderPosition).toInt()
                    IntOffset(x = xPx - 20.dp.roundToPx(), y = 0)
                }
                .size(40.dp)
                .background(handleColor, shape = CircleShape)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = "Drag to compare",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A [Shape] that clips its content to only the
 * rectangle from x=0 to x=(width * revealFraction), used to mask the "before"
 * image layer so it only shows up to the current divider position.
 */
private class BeforeImageRevealShape(
    private val revealFraction: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val revealedWidth = size.width * revealFraction
        return Outline.Rectangle(
            Rect(
                left = 0f,
                top = 0f,
                right = revealedWidth,
                bottom = size.height
            )
        )
    }
}
