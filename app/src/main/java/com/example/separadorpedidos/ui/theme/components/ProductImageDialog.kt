// IMPORTANTE: BACKUP DO ARQUIVO ORIGINAL
// Salve este conteúdo em algum lugar seguro antes de substituir o arquivo ProductImageDialog.kt

// Esta é uma versão do ProductImageDialog.kt original que mantém apenas as funções auxiliares
// relevantes, removendo a definição conflitante do componente Composable principal

package com.example.separadorpedidos.ui.components

// Importe apenas o necessário para as funções auxiliares
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import java.io.ByteArrayOutputStream

// Remova a definição do composable ProductImageDialog principal
// Este é o trecho problemático que causa conflito com ProductImageDialogBase64

// Mantenha apenas funções úteis para processamento de imagens
// que podem ser reutilizadas pelo novo componente

// Função auxiliar para converter bitmap para base64
fun bitmapToBase64Legacy(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
    val imageBytes = outputStream.toByteArray()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}