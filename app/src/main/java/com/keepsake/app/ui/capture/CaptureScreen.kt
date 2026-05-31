package com.keepsake.app.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.keepsake.app.data.repository.PhotoRepository
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import android.graphics.BitmapFactory
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    areaId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var capturedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    ) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) showPermissionDenied = true
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拍照") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Camera preview
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = androidx.camera.core.Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        context as androidx.lifecycle.LifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) { /* ignore */ }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("需要相机权限", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("授予权限")
                        }
                    }
                }
            }

            // Gallery picker button
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val galleryLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetMultipleContents()
                ) { uris -> capturedUris = capturedUris + uris }

                val cameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicture()
                ) { success ->
                    // Handled by ImageCapture
                }

                FilledTonalButton(onClick = {
                    if (hasCameraPermission) {
                        // Capture via ImageCapture
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.Camera, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("拍照")
                }
                FilledTonalButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("相册")
                }
            }

            // Preview grid
            if (capturedUris.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(capturedUris.toList()) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { capturedUris = capturedUris - uri },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Save button
                Button(
                    onClick = {
                        // TODO: inject PhotoRepository via hiltViewModel
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("保存")
                }
            }
        }
    }

    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text("需要相机权限") },
            text = { Text("请在系统设置中授予相机权限后重试") },
            confirmButton = { TextButton(onClick = { showPermissionDenied = false }) { Text("确定") } }
        )
    }
}
