// Arquivo: app/src/main/java/com/example/separadorpedidos/ui/theme/components/ProductImageDialogBase64.kt

package com.example.separadorpedidos.ui.components

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.separadorpedidos.data.api.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Cache de imagens para evitar recarregamentos desnecessários
private val imageCache = mutableMapOf<String, Bitmap>()

@Composable
fun ProductImageDialogBase64(
    isVisible: Boolean,
    codigoProduto: String,
    productName: String,
    onDismiss: () -> Unit
) {
    var imageStateBase64 by remember { mutableStateOf<ImageStateBase64>(ImageStateBase64.Loading) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var debugInfo by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Função para criar um arquivo de imagem temporário
    val createImageFile = {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    // URI para a imagem capturada - Usando mutableStateOf simples em vez de remember
    var photoUriState = remember { mutableStateOf<Uri?>(null) }

    // Funções para processamento de imagem
    val processImage: (Bitmap) -> Bitmap = { originalBitmap ->
        // Calcular o novo tamanho mantendo a proporção
        val maxDimension = 1800 // Dimensão máxima para um bom equilíbrio qualidade/tamanho
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height

        var newWidth: Int
        var newHeight: Int

        if (originalWidth > originalHeight) {
            newWidth = maxDimension
            newHeight = (originalHeight.toFloat() / originalWidth.toFloat() * maxDimension).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (originalWidth.toFloat() / originalHeight.toFloat() * maxDimension).toInt()
        }

        // Redimensionar para o novo tamanho
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

        // Se o bitmap original é muito grande, recicle-o para liberar memória
        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            originalBitmap.recycle()
        }

        resizedBitmap
    }

    // Launcher para captura de foto com resolução alta
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && photoUriState.value != null) { // Usando .value para acessar o Uri
            try {
                // Processar a imagem capturada
                val inputStream = context.contentResolver.openInputStream(photoUriState.value!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    isUploading = true
                    uploadError = null
                    debugInfo = "Iniciando upload da foto de alta qualidade..."

                    // Processar e redimensionar a imagem, mas manter qualidade razoável
                    val processedBitmap = processImage(bitmap)

                    uploadImageBase64(codigoProduto, processedBitmap) { successUpload, errorMsg ->
                        debugInfo = if (successUpload) "Upload concluído com sucesso!"
                        else "Erro no upload: $errorMsg"

                        if (successUpload) {
                            // Recarrega a imagem após upload
                            loadImageBase64(codigoProduto) { newState ->
                                imageStateBase64 = newState
                            }
                        } else {
                            uploadError = errorMsg
                        }
                        isUploading = false
                    }
                } else {
                    uploadError = "Falha ao processar a imagem capturada"
                    debugInfo = "Bitmap nulo após captura"
                    isUploading = false
                }
            } catch (e: Exception) {
                uploadError = "Erro ao processar a imagem: ${e.message}"
                debugInfo = "Exceção: ${e.javaClass.simpleName}"
                isUploading = false
            }
        } else if (!success) {
            uploadError = "Captura de imagem cancelada"
            debugInfo = "Usuário cancelou a captura"
        }
    }

    // Launcher para permissão de câmera
    // Launcher para permissão de câmera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                // Criar arquivo temporário e URI
                val photoFile = createImageFile()
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )

                // Armazenar o URI no estado e iniciar a câmera
                photoUriState.value = uri
                // Agora que temos certeza que o URI não é nulo, podemos passá-lo para o launcher
                takePhotoLauncher.launch(uri)  // Passando um URI não-nulo diretamente

            } catch (e: Exception) {
                uploadError = "Erro ao preparar a câmera: ${e.message}"
                debugInfo = "Exceção: ${e.javaClass.simpleName}"
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Carrega a imagem quando o dialog abre
    LaunchedEffect(isVisible, codigoProduto) {
        if (isVisible) {
            imageStateBase64 = ImageStateBase64.Loading
            uploadError = null
            debugInfo = "Carregando..."
            loadImageBase64(codigoProduto) { newState ->
                imageStateBase64 = newState
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
                            if (debugInfo.isNotEmpty()) {
                                Text(
                                    text = debugInfo,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
                        val currentState = imageStateBase64
                        when (currentState) {
                            is ImageStateBase64.Loading -> {
                                LoadingContentBase64()
                            }
                            is ImageStateBase64.HasImage -> {
                                ImageContentBase64(
                                    imageBitmap = currentState.bitmap,
                                    productName = productName,
                                    onTakeNewPhoto = {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    isUploading = isUploading,
                                    uploadError = uploadError
                                )
                            }
                            is ImageStateBase64.NoImage -> {
                                NoImageContentBase64(
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
                            is ImageStateBase64.Error -> {
                                ErrorContentBase64(
                                    error = currentState.error,
                                    onRetry = {
                                        debugInfo = "Tentando novamente..."
                                        loadImageBase64(codigoProduto) { newState ->
                                            imageStateBase64 = newState
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

sealed class ImageStateBase64 {
    object Loading : ImageStateBase64()
    data class HasImage(val bitmap: Bitmap) : ImageStateBase64()
    object NoImage : ImageStateBase64()
    data class Error(val error: String) : ImageStateBase64()
}

// Conteúdo dos estados
@Composable
private fun LoadingContentBase64() {
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
private fun NoImageContentBase64(
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
            text = "Tire uma foto para cadastrar este produto",
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
                    "📷 Tirar Foto do Produto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ImageContentBase64(
    imageBitmap: Bitmap,
    productName: String,
    onTakeNewPhoto: () -> Unit,
    isUploading: Boolean,
    uploadError: String?
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagem - usando imageBitmap ao invés de URL
        Image(
            bitmap = imageBitmap.asImageBitmap(),
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
private fun ErrorContentBase64(
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

// FUNÇÃO CORRIGIDA - Com verificação de cache e sem referências não resolvidas
private fun loadImageBase64(codigoProduto: String, onResult: (ImageStateBase64) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Verificar o cache primeiro
            val cachedBitmap = imageCache[codigoProduto]
            if (cachedBitmap != null) {
                withContext(Dispatchers.Main) {
                    onResult(ImageStateBase64.HasImage(cachedBitmap))
                }
                return@launch
            }

            Log.d("ImageDialog", "Chamando API para produto: $codigoProduto")
            // Faz a chamada para a API
            val response = NetworkModule.apiService.getProductImage(codigoProduto)

            Log.d("ImageDialog", "Resposta da API - Code: ${response.code()}")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val imageResponse = response.body()

                    // Log detalhes da resposta
                    Log.d("ImageDialog", "Success: ${imageResponse?.success}")
                    Log.d("ImageDialog", "Base64 presente: ${!imageResponse?.imageBase64.isNullOrEmpty()}")

                    if (imageResponse?.success == true && !imageResponse.imageBase64.isNullOrEmpty()) {
                        // Verificar se o Base64 tem prefixo de data URL
                        var base64Data = imageResponse.imageBase64
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1)
                        }

                        try {
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            Log.d("ImageDialog", "Base64 decodificado, bytes: ${imageBytes.size}")

                            val loadedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                            if (loadedBitmap != null) {
                                Log.d("ImageDialog", "Bitmap criado: ${loadedBitmap.width}x${loadedBitmap.height}")
                                // Adicionar ao cache
                                imageCache[codigoProduto] = loadedBitmap
                                onResult(ImageStateBase64.HasImage(loadedBitmap))
                            } else {
                                Log.e("ImageDialog", "Falha ao criar Bitmap dos bytes")
                                onResult(ImageStateBase64.Error("Falha ao criar imagem dos dados recebidos"))
                            }
                        } catch (e: Exception) {
                            Log.e("ImageDialog", "Erro ao processar Base64", e)
                            onResult(ImageStateBase64.Error("Erro ao processar dados da imagem: ${e.message}"))
                        }
                    } else {
                        // Sem imagem
                        Log.d("ImageDialog", "Imagem não existe ou success=false")
                        onResult(ImageStateBase64.NoImage)
                    }
                } else {
                    // Erro HTTP
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ImageDialog", "Erro HTTP ${response.code()}: $errorBody")
                    } catch (e: Exception) {
                        Log.e("ImageDialog", "Erro ao ler corpo do erro", e)
                    }
                    onResult(ImageStateBase64.Error("Erro na comunicação: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("ImageDialog", "Exceção na requisição", e)
            withContext(Dispatchers.Main) {
                onResult(ImageStateBase64.Error("Erro de conexão: ${e.message}"))
            }
        }
    }
}

private fun uploadImageBase64(
    codigoProduto: String,
    bitmap: Bitmap,
    onResult: (Boolean, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d("ImageDialog", "Iniciando upload para produto: $codigoProduto")

            // Converte bitmap para base64 com alta qualidade
            val base64 = bitmapToBase64(bitmap)
            Log.d("ImageDialog", "Imagem convertida para base64. Tamanho: ${base64.length}")

            // Cria request com a estrutura correta para a nova API
            val requestBody = mapOf(
                "codigoProduto" to codigoProduto,
                "imageBase64" to base64,
                "extensao" to "jpg"
            )

            Log.d("ImageDialog", "Fazendo upload para VKSEPALMUPLOAD...")
            // Faz upload para a nova API
            val response = NetworkModule.apiService.uploadProductImageNew(requestBody)

            Log.d("ImageDialog", "Upload response - Code: ${response.code()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("ImageDialog", "Upload bem sucedido: ${responseBody?.message}")

                // Adicionar ao cache após upload bem-sucedido
                imageCache[codigoProduto] = bitmap
            } else {
                Log.e("ImageDialog", "Erro no upload: ${response.code()}")
                try {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ImageDialog", "Corpo do erro: $errorBody")
                } catch (e: Exception) {
                    Log.e("ImageDialog", "Erro ao ler corpo do erro", e)
                }
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
    // Aumentar a qualidade de 85% para 95%
    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
    val imageBytes = outputStream.toByteArray()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}