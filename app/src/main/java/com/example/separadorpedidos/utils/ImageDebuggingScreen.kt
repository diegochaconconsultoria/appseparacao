package com.example.separadorpedidos.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.separadorpedidos.data.api.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Componente para testar a exibição de imagens Base64
 * Use esta tela para depurar e verificar se a decodificação de Base64 está funcionando
 */
@Composable
fun ImageDebuggingScreen() {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Estado para controlar a resposta da API
    var apiResponse by remember { mutableStateOf<String?>(null) }
    var base64Data by remember { mutableStateOf<String?>(null) }
    var decodedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var productCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Teste de Imagens Base64",
            style = MaterialTheme.typography.headlineMedium
        )

        // Campo para código do produto
        OutlinedTextField(
            value = productCode,
            onValueChange = { productCode = it },
            label = { Text("Código do Produto") },
            modifier = Modifier.fillMaxWidth()
        )

        // Botão para buscar imagem
        Button(
            onClick = {
                if (productCode.isNotBlank()) {
                    isLoading = true
                    errorMessage = null
                    apiResponse = null
                    base64Data = null
                    decodedBitmap = null

                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                NetworkModule.apiService.getProductImage(productCode)
                            }

                            if (response.isSuccessful) {
                                val imageResponse = response.body()
                                apiResponse = "Código: ${response.code()}\nSuccess: ${imageResponse?.success}"

                                if (imageResponse?.success == true && !imageResponse.imageBase64.isNullOrEmpty()) {
                                    base64Data = imageResponse.imageBase64

                                    // Tentar decodificar o Base64
                                    withContext(Dispatchers.Default) {
                                        try {
                                            // Verificar e remover prefixo se existir
                                            var cleanBase64 = base64Data ?: ""
                                            if (cleanBase64.contains(",")) {
                                                cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1)
                                            }

                                            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                            decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                        } catch (e: Exception) {
                                            errorMessage = "Erro ao decodificar Base64: ${e.message}"
                                            Log.e("ImageDebug", "Erro na decodificação", e)
                                        }
                                    }
                                } else {
                                    errorMessage = "Imagem não encontrada: ${imageResponse?.message}"
                                }
                            } else {
                                apiResponse = "Erro: ${response.code()}"
                                errorMessage = "Falha na requisição: ${response.errorBody()?.string()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erro de conexão: ${e.message}"
                            Log.e("ImageDebug", "Erro na requisição", e)
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = productCode.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Carregando...")
            } else {
                Text("Buscar Imagem")
            }
        }

        // Mostrar resultado da API
        apiResponse?.let {
            Text(
                text = "Resposta da API:",
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Mostrar Base64 (primeiros 100 caracteres)
        base64Data?.let {
            Text(
                text = "Base64 (primeiros 100 caracteres):",
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it.take(100) + "...",
                    modifier = Modifier.padding(8.dp)
                )
            }

            Button(
                onClick = {
                    // Copiar Base64 completo para o log
                    Log.d("ImageDebug", "Base64 completo: $it")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Base64 Completo")
            }
        }

        // Mostrar erro
        errorMessage?.let {
            Text(
                text = "Erro:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Mostrar imagem decodificada
        decodedBitmap?.let { bitmap ->
            Text(
                text = "Imagem Decodificada (${bitmap.width}x${bitmap.height}):",
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagem Decodificada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Função para mostrar a tela de depuração de imagens
 */
@Composable
fun ShowImageDebuggingDialog(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (show) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                shape = MaterialTheme.shapes.large
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Depuração de Imagens",
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = onDismiss) {
                            // Ícone X para fechar
                            Text("X")
                        }
                    }
                    Divider()
                    ImageDebuggingScreen()
                }
            }
        }
    }
}