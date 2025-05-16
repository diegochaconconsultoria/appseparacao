@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.separadorpedidos.ui.components

import android.Manifest
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
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
import com.example.separadorpedidos.data.api.NetworkModule
import com.example.separadorpedidos.data.model.ImageUploadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@Composable
fun ProductImageDialog(
    isVisible: Boolean,
    codigoProduto: String,
    productName: String,
    onDismiss: () -> Unit
) {
    var imageState by remember { mutableStateOf<ImageState>(ImageState.Loading) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }

    // Launcher para captura de foto
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            isUploading = true
            uploadError = null
            debugInfo = "Iniciando upload..."
            uploadImage(codigoProduto, it) { success, error ->
                debugInfo = if (success) "Upload concluído!" else "Erro no upload: $error"
                if (success) {
                    // Recarrega a imagem após upload
                    loadImage(codigoProduto) { newState ->
                        imageState = newState
                    }
                } else {
                    uploadError = error
                }
                isUploading = false
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
            showPermissionDialog = true
        }
    }

    // Carrega a imagem quando o dialog abre
    LaunchedEffect(isVisible, codigoProduto) {
        if (isVisible) {
            imageState = ImageState.Loading
            uploadError = null
            debugInfo = "Carregando..."
            loadImage(codigoProduto) { newState ->
                imageState = newState
                debugInfo = "Estado atual: ${newState.javaClass.simpleName}"
            }
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = !isUploading,
                dismissOnClickOutside = !isUploading,
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
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = productName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Código: $codigoProduto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Debug info
                            Text(
                                text = debugInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isUploading
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Divider()

                    // Content baseado no estado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val currentState = imageState
                        when (currentState) {
                            is ImageState.Loading -> {
                                LoadingContent()
                            }
                            is ImageState.HasImage -> {
                                ImageContent(
                                    imageUrl = currentState.imageUrl,
                                    productName = productName,
                                    onTakeNewPhoto = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    isUploading = isUploading,
                                    uploadError = uploadError
                                )
                            }
                            is ImageState.NoImage -> {
                                NoImageContent(
                                    onTakePhoto = {
                                        debugInfo = "Abrindo câmera..."
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    isUploading = isUploading,
                                    uploadError = uploadError,
                                    productName = productName,
                                    codigoProduto = codigoProduto
                                )
                            }
                            is ImageState.Error -> {
                                ErrorContent(
                                    error = currentState.error,
                                    onRetry = {
                                        debugInfo = "Tentando novamente..."
                                        loadImage(codigoProduto) { newState ->
                                            imageState = newState
                                            debugInfo = "Estado atual: ${newState.javaClass.simpleName}"
                                        }
                                    },
                                    onTakePhoto = {
                                        debugInfo = "Abrindo câmera (após erro)..."
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog de permissão
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permissão Necessária") },
            text = { Text("Para tirar fotos dos produtos, precisamos de acesso à câmera.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Permitir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

sealed class ImageState {
    object Loading : ImageState()
    data class HasImage(val imageUrl: String) : ImageState()
    object NoImage : ImageState()
    data class Error(val error: String) : ImageState()
}

// Conteúdo dos estados
@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Text(
            text = "Verificando imagem...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun NoImageContent(
    onTakePhoto: () -> Unit,
    isUploading: Boolean,
    uploadError: String?,
    productName: String,
    codigoProduto: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "📸 Nenhuma foto encontrada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tire uma foto para documentar este produto",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Informações do produto
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📦 Produto: $productName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "🏷️ Código: $codigoProduto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Mensagem de erro se houver
        uploadError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "❌ $error",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Botão principal para tirar foto
        Button(
            onClick = onTakePhoto,
            enabled = !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("📤 Enviando foto...")
            } else {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "📷 Tirar Foto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ImageContent(
    imageUrl: String,
    productName: String,
    onTakeNewPhoto: () -> Unit,
    isUploading: Boolean,
    uploadError: String?
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagem
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
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

        // Mensagem de erro se houver
        uploadError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Botão para nova foto
        Button(
            onClick = onTakeNewPhoto,
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviando...")
            } else {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tirar Nova Foto")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onTakePhoto: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "Erro ao verificar imagem",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Dois botões: Retry e Tirar Foto
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tentar Novamente")
            }

            Button(
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tirar Foto")
            }
        }
    }
}

// Funções para carregar e fazer upload das imagens
private fun loadImage(codigoProduto: String, onResult: (ImageState) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("ImageDialog", "Chamando API para produto: $codigoProduto")
            // Faz a chamada para a API
            val response = NetworkModule.apiService.getProductImage(codigoProduto)

            Log.d("ImageDialog", "Resposta da API - Code: ${response.code()}")
            Log.d("ImageDialog", "Content-Type: ${response.headers()["Content-Type"]}")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val contentType = response.headers()["Content-Type"]

                    if (contentType?.startsWith("image/") == true) {
                        // É uma imagem
                        val baseUrl = "https://192.168.0.251:8409/rest/VKSEPALMIMG/"
                        val imageUrl = "$baseUrl$codigoProduto"
                        Log.d("ImageDialog", "Imagem encontrada. URL: $imageUrl")
                        onResult(ImageState.HasImage(imageUrl))
                    } else {
                        // É JSON (imagem não existe)
                        Log.d("ImageDialog", "Imagem não existe, permitindo tirar foto")
                        onResult(ImageState.NoImage)
                    }
                } else {
                    // Erro HTTP - mas vamos permitir tirar foto mesmo assim
                    Log.w("ImageDialog", "Erro HTTP ${response.code()}, permitindo tirar foto")
                    onResult(ImageState.NoImage)
                }
            }
        } catch (e: Exception) {
            Log.e("ImageDialog", "Erro ao carregar imagem", e)
            withContext(Dispatchers.Main) {
                // Erro de rede - permite tirar foto
                onResult(ImageState.NoImage)
            }
        }
    }
}

private fun uploadImage(
    codigoProduto: String,
    bitmap: Bitmap,
    onResult: (Boolean, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("ImageDialog", "Iniciando upload para produto: $codigoProduto")

            // Converte bitmap para base64
            val base64 = bitmapToBase64(bitmap)
            Log.d("ImageDialog", "Imagem convertida para base64. Tamanho: ${base64.length}")

            // Cria request
            val request = ImageUploadRequest(
                imageBase64 = base64,
                extensao = "jpg"
            )

            Log.d("ImageDialog", "Fazendo upload...")
            // Faz upload
            val response = NetworkModule.apiService.uploadProductImage(codigoProduto, request)

            Log.d("ImageDialog", "Upload response - Code: ${response.code()}")
            if (response.isSuccessful) {
                Log.d("ImageDialog", "Response body: ${response.body()}")
            } else {
                Log.e("ImageDialog", "Upload error: ${response.errorBody()?.string()}")
            }

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, "Erro no upload: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("ImageDialog", "Erro no upload", e)
            withContext(Dispatchers.Main) {
                onResult(false, "Erro de conexão: ${e.message}")
            }
        }
    }
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // 85% quality
    val imageBytes = outputStream.toByteArray()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT).replace("\n", "")
}