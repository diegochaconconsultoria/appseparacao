@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.separadorpedidos.ui.components

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.separadorpedidos.utils.LocalImageStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProductImageDialog(
    isVisible: Boolean,
    codigoProduto: String,
    productName: String,
    onDismiss: () -> Unit,
    onImageUploaded: (String) -> Unit = {}
) {
    var imagePath by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var hasImage by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPermissionRationale by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Launcher para captura de foto
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isSaving = true
            errorMessage = ""

            // Salvar bitmap localmente
            CoroutineScope(Dispatchers.IO).launch {
                val savedPath = LocalImageStorage.saveImage(context, codigoProduto, it)

                CoroutineScope(Dispatchers.Main).launch {
                    if (savedPath != null) {
                        imagePath = savedPath
                        hasImage = true
                        isSaving = false
                        onImageUploaded(savedPath)
                    } else {
                        isSaving = false
                        errorMessage = "Erro ao salvar imagem"
                    }
                }
            }
        }
    }

    // Launcher para permissão de câmera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoLauncher.launch(null)
        } else {
            showPermissionRationale = true
        }
    }

    // Verificar se imagem existe ao abrir o dialog
    LaunchedEffect(isVisible, codigoProduto) {
        if (isVisible) {
            isLoading = true
            errorMessage = ""

            CoroutineScope(Dispatchers.IO).launch {
                hasImage = LocalImageStorage.imageExists(context, codigoProduto)
                if (hasImage) {
                    imagePath = LocalImageStorage.getImagePath(context, codigoProduto) ?: ""
                }
                CoroutineScope(Dispatchers.Main).launch {
                    isLoading = false
                }
            }
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = !isSaving,
                dismissOnClickOutside = !isSaving,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header com título e botão fechar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSaving
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider()

                    // Container principal do conteúdo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            // Estado de carregamento inicial
                            isLoading -> {
                                LoadingContent(message = "Verificando imagem...")
                            }

                            // Estado de salvamento
                            isSaving -> {
                                LoadingContent(message = "Salvando imagem...")
                            }

                            // Mostrar erro de permissão
                            showPermissionRationale -> {
                                PermissionRationaleContent(
                                    onRequestPermission = {
                                        showPermissionRationale = false
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    onDismiss = { showPermissionRationale = false }
                                )
                            }

                            // Imagem existe - mostrar imagem com opção de nova foto
                            hasImage -> {
                                ImageWithActionsContent(
                                    imagePath = imagePath,
                                    productName = productName,
                                    onTakeNewPhoto = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    onDeleteImage = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            LocalImageStorage.deleteImage(context, codigoProduto)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                hasImage = false
                                                imagePath = ""
                                            }
                                        }
                                    },
                                    errorMessage = errorMessage
                                )
                            }

                            // Sem imagem - mostrar opção de tirar foto
                            else -> {
                                NoImageContent(
                                    onTakePhoto = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    errorMessage = errorMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            Icons.Default.Camera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Permissão Necessária",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Para tirar fotos dos produtos, precisamos de acesso à câmera do dispositivo.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.weight(1f)
            ) {
                Text("Permitir")
            }
        }
    }
}

@Composable
private fun ImageWithActionsContent(
    imagePath: String,
    productName: String,
    onTakeNewPhoto: () -> Unit,
    onDeleteImage: () -> Unit,
    errorMessage: String
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagem - usando file:// para carregar imagem local
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file://$imagePath")
                .crossfade(true)
                .build(),
            contentDescription = "Imagem do produto $productName",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar erro se houver
        if (errorMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botões de ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botão para tirar nova foto
            Button(
                onClick = onTakeNewPhoto,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Foto")
            }

            // Botão para deletar imagem
            OutlinedButton(
                onClick = onDeleteImage,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deletar")
            }
        }
    }
}

@Composable
private fun NoImageContent(
    onTakePhoto: () -> Unit,
    errorMessage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Imagem não encontrada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tire uma foto para este produto",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Mostrar erro se houver
        if (errorMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Tirar Foto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}