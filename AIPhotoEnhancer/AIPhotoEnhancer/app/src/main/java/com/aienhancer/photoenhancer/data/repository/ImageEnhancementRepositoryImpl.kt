package com.aienhancer.photoenhancer.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.domain.model.ImageTask
import com.aienhancer.photoenhancer.domain.model.TaskStatus
import com.aienhancer.photoenhancer.domain.repository.ImageEnhancementRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Production-shaped implementation of [ImageEnhancementRepository].
 *
 * In a real product, the body of [enhanceImage] would dispatch to an on-device
 * ML runtime (TensorFlow Lite / ONNX / NNAPI delegate) or a remote inference
 * endpoint. Swapping in a real model is intentionally isolated to the private
 * [runPipelineStage] / [decodeAndPersistOutput] functions below - the public
 * Flow-based contract and the staged-progress emission model do not need to
 * change when the simulated stages are replaced with real inference calls.
 *
 * The simulation is deliberately not instantaneous: it walks through discrete
 * named stages (Analyze -> Denoise/Upscale/Sharpen -> Finalize), emitting a
 * [TaskStatus.Processing] update with incrementing progress at each step, which
 * is what powers the circular progress indicator and stage label on the
 * Enhance screen.
 */
@Singleton
class ImageEnhancementRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageEnhancementRepository {

    private val taskHistory = MutableStateFlow<List<ImageTask>>(emptyList())

    override fun enhanceImage(
        sourceImageUri: String,
        enhancementType: EnhancementType
    ): Flow<ImageTask> = callbackFlow {
        val task = ImageTask(
            sourceImageUri = sourceImageUri,
            enhancementType = enhancementType,
            status = TaskStatus.Idle
        )
        send(task)

        val startTime = System.currentTimeMillis()
        val stages = buildPipelineStages(enhancementType)
        val stageWeight = 1f / stages.size

        try {
            stages.forEachIndexed { index, stage ->
                // Simulate the time a real model inference / upscaling pass would take.
                val stageDuration = enhancementType.basePipelineDurationMs / stages.size
                val steps = 10
                repeat(steps) { stepIndex ->
                    kotlinx.coroutines.delay(stageDuration / steps)
                    val intraStageProgress = (stepIndex + 1) / steps.toFloat()
                    val overallProgress = (index * stageWeight) + (intraStageProgress * stageWeight)
                    send(
                        task.copy(
                            status = TaskStatus.Processing(
                                progress = overallProgress.coerceIn(0f, 0.99f),
                                stage = stage
                            )
                        )
                    )
                }
            }

            val outputUri = withContext(Dispatchers.IO) {
                decodeAndPersistOutput(sourceImageUri, enhancementType)
            }

            val duration = System.currentTimeMillis() - startTime
            val finalTask = task.copy(
                status = TaskStatus.Success(
                    outputUri = outputUri,
                    processingDurationMs = duration
                )
            )
            send(finalTask)
            taskHistory.update { current -> listOf(finalTask) + current }
        } catch (cancellation: kotlinx.coroutines.CancellationException) {
            send(task.copy(status = TaskStatus.Cancelled))
            throw cancellation
        } catch (throwable: Throwable) {
            val failedTask = task.copy(
                status = TaskStatus.Error(
                    message = throwable.message ?: "Enhancement failed due to an unknown error.",
                    cause = throwable
                )
            )
            send(failedTask)
            taskHistory.update { current -> listOf(failedTask) + current }
       awaitClose { } }
    }.flowOn(Dispatchers.Default)

    /**
     * Builds the ordered list of human-readable stage labels for a given
     * enhancement type. Different operations have different pipelines:
     * upscaling has an extra "Reconstructing detail" stage that denoise/sharpen
     * don't need, for example.
     */
    private fun buildPipelineStages(enhancementType: EnhancementType): List<String> {
        return when (enhancementType) {
            EnhancementType.UPSCALE_4X -> listOf(
                "Analyzing image",
                "Upscaling resolution",
                "Reconstructing detail",
                "Finalizing"
            )
            EnhancementType.UPSCALE_8X -> listOf(
                "Analyzing image",
                "Upscaling resolution",
                "Reconstructing detail",
                "Enhancing micro-textures",
                "Finalizing"
            )
            EnhancementType.DENOISE -> listOf(
                "Analyzing noise profile",
                "Removing noise",
                "Finalizing"
            )
            EnhancementType.SHARPNESS -> listOf(
                "Analyzing edges",
                "Sharpening details",
                "Finalizing"
            )
            EnhancementType.BATCH_PROCESSING -> listOf(
                "Queuing images",
                "Analyzing batch",
                "Processing batch",
                "Finalizing"
            )
        }
    }

    /**
     * Simulates producing an enhanced output image. Since there is no real AI
     * model wired in for this scaffold, this decodes the source bitmap and
     * (for upscale operations) actually scales it up using Android's built-in
     * bitmap scaling, then writes the result to app-specific cache storage and
     * returns a file:// URI. This means the Before/After slider on the Enhance
     * screen will show a genuinely different (upscaled / re-encoded) image,
     * not just the same file copied byte-for-byte.
     */
    private fun decodeAndPersistOutput(
        sourceImageUri: String,
        enhancementType: EnhancementType
    ): String {
        val inputStream = context.contentResolver.openInputStream(Uri.parse(sourceImageUri))
            ?: throw IllegalStateException("Unable to open source image stream")

        val original = inputStream.use { BitmapFactory.decodeStream(it) }
            ?: throw IllegalStateException("Unable to decode source image")

        val scaleFactor = when (enhancementType) {
            EnhancementType.UPSCALE_4X -> 1.5f // capped for memory safety in this demo pipeline
            EnhancementType.UPSCALE_8X -> 2.0f
            EnhancementType.DENOISE,
            EnhancementType.SHARPNESS,
            EnhancementType.BATCH_PROCESSING -> 1.0f
        }

        val processed: Bitmap = if (scaleFactor != 1.0f) {
            val newWidth = (original.width * scaleFactor).roundToInt()
            val newHeight = (original.height * scaleFactor).roundToInt()
            Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        } else {
            original
        }

        val outputDir = File(context.cacheDir, "enhanced_images").apply { mkdirs() }
        val outputFile = File(outputDir, "enhanced_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { stream: OutputStream ->
            processed.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        }

        if (processed !== original) {
            original.recycle()
        }

        return Uri.fromFile(outputFile).toString()
    }

    override suspend fun exportToGallery(enhancedImageUri: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sourceUri = Uri.parse(enhancedImageUri)
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                    ?: throw IllegalStateException("Unable to read enhanced image for export")

                val fileName = "AIPhotoEnhancer_${System.currentTimeMillis()}.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AIPhotoEnhancer")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val resultUri = context.contentResolver.insert(collection, contentValues)
                    ?: throw IllegalStateException("Unable to create MediaStore entry")

                context.contentResolver.openOutputStream(resultUri)?.use { out ->
                    inputStream.use { input -> input.copyTo(out) }
                } ?: throw IllegalStateException("Unable to open output stream for export")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(resultUri, contentValues, null, null)
                }

                resultUri.toString()
            }
        }

    override fun observeTaskHistory(): Flow<List<ImageTask>> = taskHistory.asStateFlow()
}
