package com.aienhancer.photoenhancer.presentation.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.aienhancer.photoenhancer.R
import com.aienhancer.photoenhancer.domain.model.EnhancementType
import com.aienhancer.photoenhancer.presentation.common.FeatureGridItem
import com.aienhancer.photoenhancer.presentation.common.UnlockFeatureDialog
import kotlinx.coroutines.launch
import java.io.File

/**
 * The app's landing screen: lets the user pick a source image (gallery or
 * camera capture) and presents the grid of available AI enhancement tools.
 * Tapping a free tool with an image selected navigates straight to
 * [com.aienhancer.photoenhancer.presentation.enhance.EnhanceScreen]; tapping a
 * locked premium tool opens the rewarded-ad unlock dialog instead.
 *
 * @param onNavigateToEnhance invoked with (imageUri, enhancementType) once
 *  the user has both an image selected and an unlocked enhancement type tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEnhance: (imageUri: String, enhancementType: EnhancementType) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            pendingCameraUri?.let { viewModel.onImageSelected(it.toString()) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val uri = createCameraOutputUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.permission_camera_required))
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            ImagePickerCard(
                selectedImageUri = uiState.selectedImageUri,
                onPickGallery = {
                    galleryLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onCapturePhoto = {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            )

            Text(
                text = stringResource(R.string.features_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 28.dp, bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(EnhancementType.entries.filter { it != EnhancementType.BATCH_PROCESSING }) { type ->
                    FeatureGridItem(
                        enhancementType = type,
                        icon = type.toIcon(),
                        isLocked = uiState.isFeatureLocked(type),
                        onClick = {
                            handleFeatureTap(
                                enhancementType = type,
                                selectedImageUri = uiState.selectedImageUri,
                                isLocked = uiState.isFeatureLocked(type),
                                viewModel = viewModel,
                                onNavigateToEnhance = onNavigateToEnhance,
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    )
                }
            }
        }
    }

    uiState.pendingUnlockRequest?.let { enhancementType ->
        UnlockFeatureDialog(
            featureName = enhancementType.displayName,
            isAdLoading = uiState.isAdLoading,
            onConfirm = viewModel::onWatchAdConfirmed,
            onDismiss = viewModel::dismissUnlockDialog
        )
    }
}

/**
 * Card showing either the "pick an image" call-to-action buttons, or a
 * preview thumbnail of the currently selected image with an option to change it.
 */
@Composable
private fun ImagePickerCard(
    selectedImageUri: String?,
    onPickGallery: () -> Unit,
    onCapturePhoto: () -> Unit
) {
    androidx.compose.material3.Card(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.height(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onPickGallery,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Collections,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.pick_from_gallery))
                }
                OutlinedButton(
                    onClick = onCapturePhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.capture_photo))
                }
            }
        }
    }
}

/** Maps each enhancement type to a representative Material icon for the grid. */
private fun EnhancementType.toIcon() = when (this) {
    EnhancementType.UPSCALE_4X -> Icons.Filled.FilterHdr
    EnhancementType.UPSCALE_8X -> Icons.Filled.Layers
    EnhancementType.DENOISE -> Icons.Filled.AutoAwesome
    EnhancementType.SHARPNESS -> Icons.Filled.Brush
    EnhancementType.BATCH_PROCESSING -> Icons.Filled.Collections
}

/**
 * Central tap-handling logic shared by every feature card: validates an image
 * is selected, defers to the ViewModel's unlock flow for locked features, and
 * navigates immediately for unlocked/free features.
 */
private fun handleFeatureTap(
    enhancementType: EnhancementType,
    selectedImageUri: String?,
    isLocked: Boolean,
    viewModel: HomeViewModel,
    onNavigateToEnhance: (String, EnhancementType) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    if (selectedImageUri == null) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Please select an image first")
        }
        return
    }

    if (isLocked) {
        viewModel.onFeatureCardTapped(enhancementType)
    } else {
        onNavigateToEnhance(selectedImageUri, enhancementType)
    }
}

/** Creates a fresh file:// (via FileProvider) URI to capture a new photo into. */
private fun createCameraOutputUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File(imagesDir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
